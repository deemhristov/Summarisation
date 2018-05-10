cmake_minimum_required(VERSION 2.8.9)

#set(CMAKE_C_COMPILER clang)
#set(CMAKE_CXX_COMPILER clang++)

#if (NOT DEFINED CMAKE_SUPPRESS_DEVELOPER_WARNINGS)
#    set(CMAKE_SUPPRESS_DEVELOPER_WARNINGS 1 CACHE INTERNAL "No dev warnings")
#endif (NOT DEFINED CMAKE_SUPPRESS_DEVELOPER_WARNINGS)

project(Summarisation)

set(CMAKE_BINARY_DIR ${CMAKE_SOURCE_DIR}/bin)
set(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR})
set(LIBRARY_OUTPUT_PATH ${CMAKE_BINARY_DIR})

include_directories(
    ${PROJECT_SOURCE_DIR}/
    ${PROJECT_SOURCE_DIR}/extract/
    ${PROJECT_SOURCE_DIR}/lib/
    ${PROJECT_SOURCE_DIR}/prepare/
)

#add_subdirectory(annotate)
add_subdirectory(lib)
add_subdirectory(prepare)
add_subdirectory(extract)
#add_subdirectory(abstract)

enable_testing()

