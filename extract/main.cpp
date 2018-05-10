#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <cstring>
#include <cwchar>
#include <cwctype>
#include <fstream>
#include <getopt.h>
#include <iostream>
#include <limits.h>
#include <set>
#include <sstream>
#include <string>
#include <unistd.h>
#include <vector>

#include "UtfConverter.h"
#include "lexrank.h"

std::vector<double> _lexrank;

void display_help()
{
    std::cerr << "Usage: extract [options] FILE" << std::endl;
    std::cerr << std::endl;
    std::cerr << "  -m, --measure=MEASURE       Set measure type." << std::endl;
    std::cerr << "                              MEASURE is cosine (default), lcs, isf-lcs, first or random." << std::endl;
    std::cerr << "  -t, --threshold=THRESHOLD   Set graph threshold." << std::endl;
    std::cerr << "                              THRESHOLD is in [0; 1] or cont|- (default)." << std::endl;
    std::cerr << "  -e, --epsilon=EPSILON       Set epsilon." << std::endl;
    std::cerr << "                              EPSILON is in [0; 1] (default is 0.01)." << std::endl;
    std::cerr << "  -d, --damping=FACTOR        Set damping factor." << std::endl;
    std::cerr << "                              FACTOR is in [0; 1] (default is 0.1)." << std::endl;
    std::cerr << "  -s, --part=SIZE             Set summary size as part." << std::endl;
    std::cerr << "                              SIZE is in [0; 1] (default is 0.2)." << std::endl;
    std::cerr << "  -S, --length=SIZE           Set summary size as word length." << std::endl;
    std::cerr << "                              SIZE is positive integer." << std::endl;
    std::cerr << "  -l, --lemma                 Lemmatize." << std::endl;
    std::cerr << "  -g, --tags=TAGS             Set POS tags to consider." << std::endl;
    std::cerr << "                              TAGS is a string of tags or all (default)." << std::endl;
    std::cerr << "  -p, --stopwords             Remove stopwords." << std::endl;
    std::cerr << "  -v, --verbose               Verbose mode." << std::endl;
    std::cerr << "  -h, --help                  Show this information." << std::endl;
    std::cerr << std::endl;
}

void load_file(std::string &s, std::istream &is)
{
    s.erase();
    s.reserve(is.rdbuf()->in_avail());
    char c;
    while (is.get(c))
    {
        if (s.capacity() == s.size())
            s.reserve(s.capacity() * 3);
        s.append(1, c);
    }
}

bool compare(int i, int j)
{
    return _lexrank[i] > _lexrank[j];
}

std::string get_stfn()
{
    char buff[PATH_MAX];
    ssize_t len = readlink("/proc/self/exe", buff, sizeof(buff)-1);
    if (len != -1) {
        buff[len] = '\0';
        char *lstslsh = strrchr(buff, '/');
        *lstslsh = '\0';
        strcat(buff, "/stopwords.txt");
        return std::string(buff);
    }
    return "none";
}

int main(int argc, char *const argv[])
{
    if (argc == 1) display_help();

    std::string measure = "cosine";
    double thres = LexRank::CONTINUOUS;
    double eps = 0.01;
    double damp = 0.1;
    double sumpart = 0.2;
    int sumlen = 0;
    bool toklem = false;
    bool verbose = false;
    std::wstring postags = L"all";
    std::string stfn = "-";
    std::string infn;

    const char *short_options = "m:t:e:d:s:S:lg:pvh";
    static struct option long_options[] =
    {
        { "measure", required_argument, 0, 'm' },
        { "threshold", required_argument, 0, 't' },
        { "epsilon", required_argument, 0, 'e' },
        { "damping", required_argument, 0, 'd' },
        { "part", required_argument, 0, 's' },
        { "length", required_argument, 0, 'S' },
        { "lemma", no_argument, 0, 'l' },
        { "tags", required_argument, 0, 'g' },
        { "stopwords", no_argument, 0, 'p' },
        { "verbose", no_argument, 0, 'v' },
        { "help", no_argument, 0, 'h' },
        { 0, 0, 0, 0 }
    };

    bool error = false;
    while (true)
    {
        const char opt = getopt_long(argc, argv, short_options, long_options, 0);
        if (opt == -1) break;

        char *end;
        switch (opt)
        {
            case 'm':
                if (strcmp(optarg, "cosine") != 0 && strcmp(optarg, "lcs") != 0 && strcmp(optarg, "isf-lcs") != 0 && strcmp(optarg, "first") != 0 && strcmp(optarg, "random") != 0)
                {
                    std::cerr << "-m MEASURE must be cosine, lcs, isf-lcs, first or random." << std::endl;
                    error = true;
                }
                else
                {
                    measure = optarg;
                    std::cerr << "-m MEASURE is set to " << measure << "." << std::endl;
                }
                break;
            case 't':
                if (strcmp(optarg, "-") == 0 || strcmp(optarg, "cont") == 0)
                {
                    thres = LexRank::CONTINUOUS;
                    std::cerr << "-t THRESHOLD is set to cont." << std::endl;
                }
                else
                {
                    thres = strtod(optarg, &end);
                    if (end == optarg || thres < 0.0 || thres > 1.0)
                    {
                        std::cerr << "-t THRESHOLD must be in [0; 1] or cont|-." << std::endl;
                        error = true;
                    }
                    else std::cerr << "-t THRESHOLD is set to " << thres << "." << std::endl;
                }
                break;
            case 'e':
                eps = strtod(optarg, &end);
                if (end == optarg || eps < 0.0 || eps > 1.0)
                {
                    std::cerr << "-e EPSILON must be in [0; 1]." << std::endl;
                    error = true;
                }
                else std::cerr << "-e EPSILON is set to " << eps << "." << std::endl;
                break;
            case 'd':
                damp = strtod(optarg, &end);
                if (end == optarg || damp < 0.0 || damp > 1.0)
                {
                    std::cerr << "-d FACTOR must be in [0; 1]." << std::endl;
                    error = true;
                }
                else std::cerr << "-d FACTOR is set to " << damp << "." << std::endl;
                break;
            case 's':
                sumpart = strtod(optarg, &end);
                if (end == optarg || sumpart < 0.0 || sumpart > 1.0)
                {
                    std::cerr << "-s SIZE must be in [0; 1]." << std::endl;
                    error = true;
                }
                else std::cerr << "-s SIZE is set to " << sumpart << "." << std::endl;
                break;
            case 'S':
                sumlen = strtol(optarg, &end, 0);
                if (end == optarg || sumlen <= 0)
                {
                    std::cerr << "-S SIZE must be positive integer." << std::endl;
                    error = true;
                }
                else std::cerr << "-S SIZE is set to " << sumlen << "." << std::endl;
                break;
            case 'l':
                toklem = true;
                std::cerr << "-l is set." << std::endl;
                break;
            case 'g':
                postags = std::wstring(UtfConverter::FromUtf8(optarg));
                std::cerr << "-g TAGS is set to " << optarg << "." << std::endl;
                break;
            case 'p':
                stfn = "stop";
                std::cerr << "-p is set." << std::endl;
                break;
            case 'v':
                verbose = true;
                std::cerr << "-v is set." << std::endl;
                break;
            case 'h':
                std::cerr << "-h Displaying help." << std::endl;
                display_help();
                return 0;
            case ':':
                std::cerr << ": : : : : : :" << std::endl;
                break;
            case '?':
                std::cerr << "? ? ? ? ? ? ?" << std::endl;
                break;
            default:
                std::cerr << "d e f a u l t" << std::endl;
                break;
        }
    }
    if (argc == optind) error = true;
    if (error) return 1;
    infn = argv[optind];

    std::fstream fs;
    std::wstring instr;
    std::wstring ststr;
    std::vector<std::vector<std::wstring>> sents;
    std::vector<std::vector<std::wstring>> sentlemmas;
    std::vector<std::vector<std::wstring>> senttags;
    std::set<std::wstring> stopwords;

    fs.open(infn, std::fstream::in);
    if (fs.fail())
    {
        std::cerr << "Cannot open " << infn << "." << std::endl;
        return 2;
    }

    std::string tmp;
    load_file(tmp, fs);
    fs.close();

    instr = UtfConverter::FromUtf8(tmp);
    std::vector<std::wstring> words;
    std::vector<std::wstring> lemmas;
    std::vector<std::wstring> tags;

    wchar_t *inbuf;
    wchar_t *ss = std::wcstok(&instr[0], L"\t\n", &inbuf);
    int col = 0;
    while (ss)
    {
        if (col == 0) words.push_back(std::wstring(ss));
        else if (col == 1) lemmas.push_back(std::wstring(ss));
        else tags.push_back(std::wstring(ss));
        col++; col %= 3;
        ss = std::wcstok(NULL, L"\t\n", &inbuf);
    }

    for (std::wstring &word : words)
    {
        if (word == L"<S>")
        {
            std::vector<std::wstring> vec;
            vec.push_back(word);
            sents.push_back(vec);
        }
        else
            sents.back().push_back(word);
    }

    for (std::wstring &lemma : lemmas)
    {
        if (lemma == L"<S>")
        {
            std::vector<std::wstring> vec;
            vec.push_back(lemma);
            sentlemmas.push_back(vec);
        }
        else
            sentlemmas.back().push_back(lemma);
    }

    bool sstart = true;
    for (std::wstring &tag : tags)
    {
        if (sstart)
        {
            std::vector<std::wstring> vec;
            vec.push_back(tag);
            senttags.push_back(vec);
            sstart = false;
        }
        else
        {
            senttags.back().push_back(tag);
            if (tag == L"X") sstart = true;
        }
    }

    if (stfn != "-" && stfn != "none")
    {
        stfn = get_stfn();
        fs.open(stfn, std::fstream::in);
        if (fs.fail())
        {
            std::cerr << "-p Cannot open " << stfn << "." << std::endl;
            return 2;
        }

        tmp = "";
        load_file(tmp, fs);
        fs.close();

        ststr = UtfConverter::FromUtf8(tmp);

        wchar_t *stbuf;
        ss = std::wcstok(&ststr[0], L"\n", &stbuf);
        while (ss)
        {
            stopwords.insert(std::wstring(ss));
            ss = std::wcstok(NULL, L"\n", &stbuf);
        }
    }

    std::vector<std::vector<std::wstring>> sents_filtered;
    if (toklem)
        for (int i = 0; i < sentlemmas.size(); i++)
        {
            std::vector<std::wstring> sentf;
            for (int j = 0; j < sentlemmas[i].size(); j++)
            {
                std::wstring &lemma = sentlemmas[i][j];
                if (!std::iswpunct(lemma[0]) && stopwords.count(lemma) == 0 && (postags == L"all" || senttags[i][j].find_first_of(postags) < std::wstring::npos))
                    sentf.push_back(lemma);
            }
            sents_filtered.push_back(sentf);
        }
    else
        for (int i = 0; i < sents.size(); i++)
        {
            std::vector<std::wstring> sentf;
            for (int j = 0; j < sents[i].size(); j++)
            {
                std::wstring &word = sents[i][j];
                if (!std::iswpunct(word[0]) && stopwords.count(word) == 0 && (postags == L"all" || senttags[i][j].find_first_of(postags) < std::wstring::npos))
                    sentf.push_back(word);
            }
            sents_filtered.push_back(sentf);
        }

    LexRank lex;
    lex.init(measure, sents_filtered);
    _lexrank = lex.get_rank(thres, eps, damp);

    if (verbose)
    {
        lex.print_debug(thres);
        for (int i = 0; i < sents.size(); i++) std::cerr << i << "\t" << _lexrank[i] << std::endl;
    }

    double maxrank = 0;
    for (double r : _lexrank)
        if (r > maxrank)
            maxrank = r;

    for (int i = 0; i < _lexrank.size(); i++)
        _lexrank[i] /= maxrank;

#ifdef test
    for (int i = 0; i < _lexrank.size(); i++)
    {
        std::cout << i << " " << _lexrank[i] << std::endl;
    }
#endif // test

    std::vector<int> lrid(_lexrank.size());
    for (int i = 0; i < lrid.size(); i++)
        lrid[i] = i;
    std::sort(lrid.begin(), lrid.end(), compare);

#ifdef test
    for (int i = 0; i < _lexrank.size(); i++)
    {
        std::cout << lrid[i] << " " << _lexrank[lrid[i]] << std::endl;
    }
#endif // test

    //double logfun = 1.0 / (1.0 + std::exp(-(double)sents.size() / 200.0));

    //sumlen = std::round(500 * logfun);
    if (sumlen == 0)
        sumlen = std::round(sumpart * (words.size() - (sents.size() * 2)));
    //std::cout << sumlen << " " << logfun << "!!!!\n";
    //std::cout << words.size() - (sents.size() * 2) << " × " << sumpart << " ~ " << sumlen << std::endl;
    int sumsize, sumstc;
    for (sumsize = 0, sumstc = 0; sumstc < sents.size(); sumstc++)
    {
        if (sumsize > sumlen)
            break;
        sumsize += sents[lrid[sumstc]].size() - 2;
    }
    std::sort(lrid.begin(), lrid.begin() + sumstc);

    //std::cout << sumstc << " (" << sumsize << ") / " << sents.size()
    //          << " (" << words.size() - (sents.size() * 2) << ")" << std::endl;
#ifdef test
    for (int i = 0; i < _lexrank.size(); i++)
    {
        std::cout << lrid[i] << " " << _lexrank[lrid[i]] << std::endl;
    }
#endif // test

    //std::cout << "Summary:" << std::endl;
    for (int i = 0; i < sumstc; i++)
    {
        bool first = true;
        for (auto word : sents[lrid[i]])
        {
            if (word == L"<S>" || word == L"</S>") continue;
            std::cout << (first ? "" : " ") << UtfConverter::ToUtf8(word);
            first = false;
        }
        std::cout << std::endl;
    }
    /*
    std::cout << std::endl;
    for (int i = 0; i < sumstc; i++)
    {
        bool first = true;
        for (auto lemma : sentlemmas[lrid[i]])
        {
            if (lemma == L"<S>" || lemma == L"</S>") continue;
            std::cout << (first ? "" : " ") << UtfConverter::ToUtf8(lemma);
            first = false;
        }
        std::cout << std::endl;
    }
    */

    return 0;
}
