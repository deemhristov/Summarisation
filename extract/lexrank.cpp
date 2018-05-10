#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <iomanip>
#include <iostream>
#include <set>
#include <string>
#include <vector>

#include "UtfConverter.h"

#include "lexrank.h"

void LexRank::init(const std::string & measure,
                   const std::vector<std::vector<std::wstring>> & sents)
{
    if (measure == "first")
    {
        _baseline = 1;
        _sentences = sents;
        return;
    }
    else if (measure == "random")
    {
        _baseline = 2;
        _sentences = sents;
        return;
    }
    std::set<std::wstring> kywds;
    for (int i = 0; i < sents.size(); i++)
        for (int j = 0; j < sents[i].size(); j++)
            kywds.insert(sents[i][j]);
    _keywords.insert(_keywords.end(), kywds.begin(), kywds.end());
    _sentences = sents;
    _similarity.resize(_sentences.size(), std::vector<double>(_sentences.size()));
    calc_tf_isf();

    for (int x = 0; x < _sentences.size(); x++)
        for (int y = x; y < _sentences.size(); y++)
        {
            double similarity = 0;
            if (measure == "cosine") similarity = cosine(x, y);
            else if (measure == "lcs") similarity = lcs(x, y);
            else if (measure == "isf-lcs") similarity = isf_lcs(x, y);
            _similarity[x][y] = _similarity[y][x] = similarity;
        }

    for (int x = 0; x < _sentences.size(); x++)
    {
        double sumsim = 0;
        for (int y = 0; y < _sentences.size(); y++)
            sumsim += _similarity[x][y];
        _sumsim.push_back(sumsim);
    }

#ifdef test
    for (int x = 0; x < _sentences.size(); x++)
    {
        std::cout << x << " " << _sumsim[x];
        for (int y = 0; y < _sentences.size(); y++)
            std::cout << " " << _similarity[x][y];
        std::cout << std::endl;
    }
#endif // test
}

double LexRank::cosine(int x, int y)
{
    double result = 0;

    for (int i : _sentkw[x])
        result += _kwtf[i][x] * _kwtf[i][y] * _kwisf[i] * _kwisf[i];

    result /= _stfisf[x];
    result /= _stfisf[y];

    return result;
}

double LexRank::lcs(int x, int y)
{
    std::vector<int> pv(_sentences[y].size() + 1, 0);
    std::vector<int> cv(_sentences[y].size() + 1, 0);
    for (int i = 0; i < _sentences[x].size(); i++)
    {
        pv = cv;
        cv[0] = 0;
        for (int j = 0; j < _sentences[y].size(); j++)
        {
            int cur = cv[j];
            if (cur < pv[j + 1]) cur = pv[j + 1];
            if (cur < pv[j] + (_sentences[x][i] == _sentences[y][j] ? 1 : 0))
                cur = pv[j] + (_sentences[x][i] == _sentences[y][j] ? 1 : 0);
            cv[j + 1] = cur;
        }
    }
    double p = cv[_sentences[y].size()] / (double)_sentences[x].size();
    double r = cv[_sentences[y].size()] / (double)_sentences[y].size();
    double f = (2 * p * r) / (p + r);
    return f;
}

double LexRank::isf_lcs(int x, int y)
{
    std::vector<double> pv(_sentences[y].size() + 1, 0.0);
    std::vector<double> cv(_sentences[y].size() + 1, 0.0);
    for (int i = 0; i < _sentences[x].size(); i++)
    {
        int pos = std::find(_keywords.begin(), _keywords.end(), _sentences[x][i]) - _keywords.begin();
        double isf = pos < _keywords.size() ? _kwisf[pos] : 0;
        //std::cerr << UtfConverter::ToUtf8(_sentences[x][i]) << " " << pos << " " << isf << std::endl;
        pv = cv;
        cv[0] = 0;
        for (int j = 0; j < _sentences[y].size(); j++)
        {
            double cur = cv[j];
            if (cur < pv[j + 1]) cur = pv[j + 1];
            if (cur < pv[j] + (_sentences[x][i] == _sentences[y][j] ? isf : 0))
                cur = pv[j] + (_sentences[x][i] == _sentences[y][j] ? isf : 0);
            cv[j + 1] = cur;
        }
    }
    double isfsumx = 0;
    double isfsumy = 0;
    for (int i = 0; i < _keywords.size(); i++)
    {
        isfsumx += _kwtf[i][x] * _kwisf[i];
        isfsumy += _kwtf[i][y] * _kwisf[i];
    }
    //std::cerr << x << " " << y << " " << cv[_sentences[y].size()] << " " << isfsumx << " " << isfsumy << std::endl;
    double p = cv[_sentences[y].size()] / isfsumx;
    double r = cv[_sentences[y].size()] / isfsumy;
    double f = (2 * p * r) / (p + r);
    return f;
}

std::vector<double> LexRank::get_rank(double thres, double eps, double damp)
{
    if (_baseline > 0)
    {
        std::vector<double> firstrank;
        for (int i = 0; i < _sentences.size(); i++)
        {
            if (_baseline == 1) firstrank.push_back((_sentences.size() - i) / (double)_sentences.size());
            else firstrank.push_back((double)rand() / (RAND_MAX));
        }
        return firstrank;
    }

    if (thres == CONTINUOUS)
        return get_rank_cont(eps, damp);

    std::vector<std::vector<int>> neigh(_sentences.size());

    for (int x = 0; x < _sentences.size(); x++)
    {
        neigh[x].push_back(x);
        for (int y = x + 1; y < _sentences.size(); y++)
            if (_similarity[x][y] >= thres)
            {
                neigh[x].push_back(y);
                neigh[y].push_back(x);
            }
    }

    std::vector<double> lexrank(_sentences.size(), 1.0 / _sentences.size());
    std::vector<double> tmp(_sentences.size());
    double delta;

    //std::cerr << "delta:" << std::endl;
    do
    {
        delta = 0;
        for (int x = 0; x < _sentences.size(); x++)
        {
            tmp[x] = damp / _sentences.size();
            for (int y : neigh[x])
                tmp[x] += (1 - damp) * lexrank[y] / neigh[y].size();
            delta += std::abs(tmp[x] - lexrank[x]);
        }
        for (int x = 0; x < _sentences.size(); x++)
            lexrank[x] = tmp[x];
        //std::cerr << std::fixed << std::showpoint << std::setprecision(16) << delta << std::endl;
    } while (delta > eps);

    return lexrank;
}

std::vector<double> LexRank::get_rank_cont(double eps, double damp)
{
    std::vector<double> lexrank(_sentences.size(), 1.0 / _sentences.size());
    std::vector<double> tmp(_sentences.size());
    double delta;

    //std::cerr << "delta:" << std::endl;
    do
    {
        delta = 0;
        for (int x = 0; x < _sentences.size(); x++)
        {
            tmp[x] = damp / _sentences.size();
            for (int y = 0; y < _sentences.size(); y++)
                tmp[x] += (1 - damp) * (_similarity[x][y] / _sumsim[y]) * lexrank[y];
            delta += std::abs(tmp[x] - lexrank[x]);
        }
        for (int x = 0; x < _sentences.size(); x++)
            lexrank[x] = tmp[x];
        //std::cerr << std::fixed << std::showpoint << std::setprecision(16) << delta << std::endl;
    } while (delta > eps);

    return lexrank;
}

void LexRank::calc_tf_isf()
{
    _sentkw.resize(_sentences.size());
    for (int i = 0; i < _keywords.size(); i++)
    {
        int sf = 0;
        std::vector<int> tf;
        for (int j = 0; j < _sentences.size(); j++)
        {
            tf.push_back(std::count(_sentences[j].begin(), _sentences[j].end(), _keywords[i]));
            if (tf.back() > 0)
            {
                sf++;
                _sentkw[j].push_back(i);
            }
        }
        _kwtf.push_back(tf);
        _kwisf.push_back(std::log((_sentences.size() + 1) / (double)(1 + sf)));
    }

    _stfisf.resize(_sentences.size());
    for (int j = 0; j < _sentences.size(); j++)
    {
        for (int i : _sentkw[j])
        {
            _stfisf[j] += _kwtf[i][j] * _kwtf[i][j] * _kwisf[i] * _kwisf[i];
        }
        _stfisf[j] = std::sqrt(_stfisf[j]);
    }
}

void LexRank::print_debug(double thres)
{
    std::cerr << std::fixed;
    for (int i = 0; i < _keywords.size(); i++)
    {
        std::cerr << "\e[32m" << std::setprecision(6) << _kwisf[i] << "\e[0m";
        for (int j = 0; j < _sentences.size(); j++)
        {
            std::cerr << " " << _kwtf[i][j];
        }
        std::cerr << " \e[36m" << UtfConverter::ToUtf8(_keywords[i]) << "\e[0m" << std::endl;
    }

    std::cerr << std::setfill('0') << "   ";
    for (int i = 0; i < _sentences.size(); i++)
        std::cerr << " \e[36m" << std::setw(3) << i << "\e[0m";
    std::cerr << std::endl;
    for (int i = 0; i < _sentences.size(); i++)
    {
        std::cerr << "\e[36m" << std::setw(3) << i << "\e[0m";
        for (int j = 0; j < _sentences.size(); j++)
        {
            std::cerr << " ";
            if (i == j) std::cerr << "\e[1m";
            if ((thres == CONTINUOUS && _similarity[i][j] > 0.0) || (thres != CONTINUOUS && _similarity[i][j] >= thres)) std::cerr << "\e[32m";
            else std::cerr << "\e[39m";
            int sim = std::lround(_similarity[i][j] * 1000);
            if (sim == 1000) std::cerr << "1.0" << "\e[0m";
            else std::cerr << std::setw(3) << sim << "\e[0m";
        }
        std::cerr << std::endl;
    }

    /*
    for (int j = 0; j < _sentences.size(); j++)
    {
        std::cerr << j << " " << _stfisf[j];
        for (int k = 0; k < _sentkw[j].size(); k++)
            std::cerr << " " << UtfConverter::ToUtf8(_keywords[_sentkw[j][k]]);
        std::cerr << std::endl;
    }
    */
}
