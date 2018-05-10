#include <string>
#include <stdexcept>
#include "UtfConverter.h"
#include "ConvertUTF.h"

#ifdef __linux__
#include "malloc.h"
#endif
#include <stdlib.h>

int strlenUtf8(const char *s);
size_t cp_strlen_utf8(const char * _s);

namespace UtfConverter
{

	std::wstring FromUtf8(const std::string& utf8string)
	{
		size_t widesize = utf8string.length();
		if (sizeof(wchar_t) == 2)
		{
			std::wstring resultstring;
			resultstring.resize(widesize+1, L'\0');
			const UTF8* sourcestart = reinterpret_cast<const UTF8*>(utf8string.c_str());
			const UTF8* sourceend = sourcestart + widesize;
			UTF16* targetstart = reinterpret_cast<UTF16*>(&resultstring[0]);
			UTF16* targetend = targetstart + widesize;
			ConversionResult res = ConvertUTF8toUTF16(&sourcestart, sourceend, &targetstart, targetend, strictConversion);
			if (res != conversionOK)
			{
				throw std::runtime_error("bad conversion !!!");
			}
			*targetstart = 0;
			return resultstring;
		}
		else if (sizeof(wchar_t) == 4)
		{
/*			
			std::wstring resultstring;
			resultstring.resize(widesize+1, L'\0');
			const UTF8* sourcestart = reinterpret_cast<const UTF8*>(utf8string.c_str());
			const UTF8* sourceend = sourcestart + widesize;
			UTF32* targetstart = reinterpret_cast<UTF32*>(&resultstring[0]);
			UTF32* targetend = targetstart + widesize;
			ConversionResult res = ConvertUTF8toUTF32(&sourcestart, sourceend, &targetstart, targetend, strictConversion);
			if (res != conversionOK)
			{
				throw std::runtime_error("bad conversion !!!");
			}
			*targetstart = 0;
			return resultstring;
 */
			int len = strlenUtf8(utf8string.c_str());
			const UTF8* sourcestart = reinterpret_cast<const UTF8*>(utf8string.c_str());
			const UTF8* sourceend = sourcestart + utf8string.length();
			int dest_size = (len + 1) * sizeof(UTF32);
			UTF32* buff = (UTF32*) malloc(dest_size);
			UTF32* dest = buff;
			UTF32* destend = dest + dest_size;
			
			ConversionResult res = ConvertUTF8toUTF32(&sourcestart, sourceend, &dest, destend, strictConversion);
			buff[len] = 0;
			
			if (res == conversionOK) {
				std::wstring resultstring;
				//resultstring = reinterpret_cast<const wchar_t*> (buff);
				resultstring = (const wchar_t*) (buff);
				free(buff);
				return resultstring;
			}
			else {
				free(buff);
				throw std::runtime_error("bad conversion !!!");
			}
			
		}
		else
		{
			throw std::runtime_error("bad conversion !!!");
		}
		return L"";
	}

	std::string ToUtf8(const std::wstring& widestring)
	{
		size_t widesize = widestring.length();

		if (sizeof(wchar_t) == 2)
		{
			size_t utf8size = 3 * widesize + 1;
			std::string resultstring;
			resultstring.resize(utf8size, '\0');
			const UTF16* sourcestart = reinterpret_cast<const UTF16*>(widestring.c_str());
			const UTF16* sourceend = sourcestart + widesize;
			UTF8* targetstart = reinterpret_cast<UTF8*>(&resultstring[0]);
			UTF8* targetend = targetstart + utf8size;
			ConversionResult res = ConvertUTF16toUTF8(&sourcestart, sourceend, &targetstart, targetend, strictConversion);
			if (res != conversionOK)
			{
				throw std::runtime_error("bad conversion !!!");
			}
			return resultstring;
		}
		else if (sizeof(wchar_t) == 4)
		{
			/*
			size_t utf8size = 4 * widesize + 1;
			std::string resultstring;
			resultstring.resize(utf8size, '\0');
			const UTF32* sourcestart = reinterpret_cast<const UTF32*>(widestring.c_str());
			const UTF32* sourceend = sourcestart + widesize;
			UTF8* targetstart = reinterpret_cast<UTF8*>(&resultstring[0]);
			UTF8* targetend = targetstart + utf8size;
			*/
			
			int len = widestring.length();
			const UTF32* sourcestart = reinterpret_cast<const UTF32*>(widestring.c_str());
			const UTF32* sourceend = sourcestart + widesize;
			int dest_size = (4* len + 1) * sizeof(UTF8);
			UTF8* buff = (UTF8*) calloc(dest_size, sizeof(UTF8));
			UTF8* dest = buff;
			UTF8* destend = dest + dest_size;
			
			ConversionResult res = ConvertUTF32toUTF8(&sourcestart, sourceend, &dest, destend, strictConversion);
			
			if (res == conversionOK){
				std::string resultstring;
				resultstring = reinterpret_cast<const char*> (buff);
				free(buff);
				return resultstring;
			}
			else {
				free(buff);
				throw std::runtime_error("bad conversion !!!");
			}
		}
		else
		{
			throw std::runtime_error("bad conversion !!!");
		}
		return "";
	}
}


int strlenUtf8(const char *s)
{
	int i = 0;
	
	//Go fast if string is only ASCII.
	//Loop while not at end of string,
	// and not reading anything with highest bit set.
	//If highest bit is set, number is negative.
	while (s[i] > 0)
		i++;
	
	if (s[i] <= -65) // all follower bytes have values below -65
		return -1; // invalid
	
	//Note, however, that the following code does *not*
	// check for invalid characters.
	//The above is just included to bail out on the tests :)
	
	int count = i;
	while (s[i])
	{
		//if ASCII just go to next character
		if (s[i] > 0)      i += 1;
		else
			//select amongst multi-byte starters
			switch (0xF0 & s[i])
		{
			case 0xE0: i += 3; break;
			case 0xF0: i += 4; break;
			default:   i += 2; break;
		}
		++count;
	}
        return count;
}



#define ONEMASK ((size_t)(-1) / 0xFF)

size_t cp_strlen_utf8(const char * _s)
{
	const char * s;
	size_t count = 0;
	size_t u;
	unsigned char b;
    
	/* Handle any initial misaligned bytes. */
	for (s = _s; (uintptr_t)(s) & (sizeof(size_t) - 1); s++) {
		b = *s;
        
		/* Exit if we hit a zero byte. */
		if (b == '\0')
			goto done;
        
		/* Is this byte NOT the first byte of a character? */
		count += (b >> 7) & ((~b) >> 6);
	}
    
	/* Handle complete blocks. */
	for (; ; s += sizeof(size_t)) {
		/* Prefetch 256 bytes ahead. */
		__builtin_prefetch(&s[256], 0, 0);
        
		/* Grab 4 or 8 bytes of UTF-8 data. */
		u = *(size_t *)(s);
        
		/* Exit the loop if there are any zero bytes. */
		if ((u - ONEMASK) & (~u) & (ONEMASK * 0x80))
			break;
        
		/* Count bytes which are NOT the first byte of a character. */
		u = ((u & (ONEMASK * 0x80)) >> 7) & ((~u) >> 6);
		count += (u * ONEMASK) >> ((sizeof(size_t) - 1) * 8);
	}
    
	/* Take care of any left-over bytes. */
	for (; ; s++) {
		b = *s;
        
		/* Exit if we hit a zero byte. */
		if (b == '\0')
			break;
        
		/* Is this byte NOT the first byte of a character? */
		count += (b >> 7) & ((~b) >> 6);
	}
    
done:
	return ((s - _s) - count);
}
