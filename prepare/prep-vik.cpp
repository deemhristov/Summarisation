#include <fstream>
#include <iostream>
#include <sstream>

#include "UtfConverter.h"

void load_file(std::string & s, std::istream & is)
{
	s.erase();
	s.reserve(is.rdbuf()->in_avail());
	char c;
	while(is.get(c))
	{
		if(s.capacity() == s.size())
			s.reserve(s.capacity() * 3);
		s.append(1, c);
	}
}

int main(int argc, char * const argv[])
{
    if (argc != 6)
    {
        std::cerr << "usage: prepare <input_file> <output_file_L> <output_file_M> <output_file_S>\n";
        return 1;
    }
    const char * infn = argv[1];
    const char * outfnT = argv[2];
    const char * outfnL = argv[3];
    const char * outfnM = argv[4];
    const char * outfnS = argv[5];

    std::fstream fs;
    std::wstring input;

    fs.open(infn, std::fstream::in);
    if (fs.fail())
    {
        std::cerr << "cannot open " << infn << "\n";
        return 2;
    }

    std::string tmp;
    load_file(tmp, fs);
    input = UtfConverter::FromUtf8(tmp);
    fs.close();

    std::wstring text = input.substr(0, input.find(L"#### SUMMARIES ####"));
    std::wstring rest = input.substr(input.find(L"#### SUMMARIES ####"));
    fs.open(outfnT, std::ios::out | std::ios::trunc);
    fs << UtfConverter::ToUtf8(text) << std::endl;
    fs.close();

    std::wstringstream ss(rest);
    std::wstringstream res;
    wchar_t line[256];
    int cid;
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;
    while (true)
    {
        ss >> cid;
        if (!ss) break;
        res << text[cid];
    }
    fs.open(outfnL, std::ios::out | std::ios::trunc);
    fs << UtfConverter::ToUtf8(res.str()) << std::endl;
    fs.close();
    ss.clear();
    res.str(L"");
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;
    while (true)
    {
        ss >> cid;
        if (!ss) break;
        res << text[cid];
    }
    fs.open(outfnM, std::ios::out | std::ios::trunc);
    fs << UtfConverter::ToUtf8(res.str()) << std::endl;
    fs.close();
    ss.clear();
    res.str(L"");
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;
    while (true)
    {
        ss >> cid;
        if (!ss) break;
        res << text[cid];
    }
    fs.open(outfnS, std::ios::out | std::ios::trunc);
    fs << UtfConverter::ToUtf8(res.str()) << std::endl;
    fs.close();
    ss.clear();
    ss.getline(line, 256);
    //std::cout << "--> " << UtfConverter::ToUtf8(line) << std::endl;

    //std::cout << UtfConverter::ToUtf8(text) << std::endl;

    return 0;
}