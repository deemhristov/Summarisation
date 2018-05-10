cmake_minimum_required(VERSION 2.8.9)

project(lib)

include_directories(
    ${CMAKE_SOURCE_DIR}/
)

# Linker options
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS}  ")

# Library path
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L\".\" ")

# Define the C sources
set (C_SRCS
    ConvertUTF.c
)

# Define the CXX sources
set (CXX_SRCS
    pugixml.cpp
    UtfConverter.cpp
)

set_source_files_properties(
    ${CXX_SRCS} PROPERTIES COMPILE_FLAGS 
    " -g -std=c++11")

set_source_files_properties(
    ${C_SRCS} PROPERTIES COMPILE_FLAGS 
    " -g")

add_library(lib ${CXX_SRCS} ${C_SRCS})

