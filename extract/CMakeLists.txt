cmake_minimum_required(VERSION 2.8.9)

project(extract)

include_directories(
    ${PROJECT_SOURCE_DIR}/
    ${PROJECT_SOURCE_DIR}/../
    ${PROJECT_SOURCE_DIR}/../lib/
)

# Linker options
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -pthread ")

# Library path
set(CMAKE_LDFLAGS "${CMAKE_LDFLAGS} -L\".\" -L\".\" ")

# Define the CXX sources
set (CXX_SRCS
    lexrank.cpp 
    main.cpp
)

set_source_files_properties(
    ${CXX_SRCS} PROPERTIES COMPILE_FLAGS
    " -g -std=c++11 -O0 -Wall -v"
)

add_executable(extract ${CXX_SRCS})

target_link_libraries(extract lib)

add_custom_command(
    TARGET extract
    POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E
    copy ${PROJECT_SOURCE_DIR}/stopwords.txt
    ${EXECUTABLE_OUTPUT_PATH}/stopwords.txt
)

enable_testing()

