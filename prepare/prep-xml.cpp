#include <cstring>
#include <fstream>
#include <iostream>

#include "pugixml.hpp"
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

void prep_xml(std::istream & is)
{
    pugi::xml_document doc;
    pugi::xml_parse_result result = doc.load(is);
    std::cerr << "Load result: " << result.description() << std::endl;

    for (pugi::xml_node xsent : doc.child("DOCUMENT").children())
    {
        std::cout << "<S>";
        bool first = true;

        for (pugi::xml_node xclause : xsent.children())
        {
            for (pugi::xml_node xword : xclause.children())
            {
                std::cout << (first ? "" : " ")
                    << ((strcmp(xword.name(), "MARKER") == 0) 
                    ? xword.child("W").text().get() : xword.text().get());
                first = false;
            }
        }
        std::cout << "</S>" << std::endl;
    }
}

int main(int argc, char * const argv[])
{
    // TODO: load file

    if (argc != 3)
	{
		std::cerr << "usage: prepare <input_format> <input_file>\n";
		return 1;
	}
    const char * inft = argv[1];
    const char * infn = argv[2];

    std::fstream fs;
	// std::wstring input;
	// std::vector<std::vector<std::wstring> > sents;

	fs.open(infn, std::fstream::in);
	if (fs.fail())
	{
		std::cerr << "cannot open " << infn << "\n";
		return 2;
	}

    if (strcmp(inft, "xml") == 0)
    {
        prep_xml(fs);
    }

	// std::string tmp;
	// load_file(tmp, fs);
	// input = UtfConverter::FromUtf8(tmp);

    fs.close();


    return 0;
}
