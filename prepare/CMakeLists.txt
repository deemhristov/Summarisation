cmake_minimum_required(VERSION 2.8.9)

project(prepare)

include_directories(
    ${CMAKE_SOURCE_DIR}/
    ${CMAKE_SOURCE_DIR}/../
    ${CMAKE_SOURCE_DIR}/lib/
)

# Linker options
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS}  ")

# Library path
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L\".\" ")

# Define the CXX sources
set (CXX_XML_SRCS
    prep-xml.cpp
)

set (CXX_VIK_SRCS
    prep-vik.cpp
)

set_source_files_properties(
    ${CXX_SRCS} PROPERTIES COMPILE_FLAGS 
    " -g -std=c++11")

add_executable(prep-xml ${CXX_XML_SRCS})
add_executable(prep-vik ${CXX_VIK_SRCS})

target_link_libraries(prep-xml lib)
target_link_libraries(prep-vik lib)
