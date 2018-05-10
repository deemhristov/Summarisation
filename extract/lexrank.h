#include <string>
#include <vector>

//#define test

class LexRank
{
public:
    static const int CONTINUOUS = -1.0;

    void init(const std::string & measure, const std::vector<std::vector<std::wstring>> & sents);
    std::vector<double> get_rank(double thres, double eps, double damp);
    void print_debug(double thres);

protected:
    std::vector<double> get_rank_cont(double eps, double damp);
    double cosine(int x, int y);
    double lcs(int x, int y);
    double isf_lcs(int x, int y);
    void calc_tf_isf();

private:
    std::vector<std::wstring> _keywords;
    std::vector<std::vector<std::wstring>> _sentences;
    std::vector<std::vector<int>> _sentkw;
    std::vector<double> _stfisf;
    std::vector<std::vector<int>> _kwtf;
    std::vector<double> _kwisf;
    std::vector<std::vector<double>> _similarity;
    std::vector<double> _sumsim;
    int _baseline = 0;
};
