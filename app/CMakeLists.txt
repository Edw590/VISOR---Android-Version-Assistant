# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

project("orb_slam2_android")

SET(CMAKE_BUILD_TYPE Release)
#SET(CMAKE_BUILD_TYPE Debug)

include(CheckCXXCompilerFlag)
CHECK_CXX_COMPILER_FLAG("-std=c++11" COMPILER_SUPPORTS_CXX11)
CHECK_CXX_COMPILER_FLAG("-std=c++0x" COMPILER_SUPPORTS_CXX0X)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os -std=c++11 -fopenmp")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
set(CMAKE_ANDROID_STL_TYPE gnustl_static)

#####################################################
# OpenCV 3.4.16
set(OpenCV_DIR "../${CMAKE_MODULE_PATH}/OpenCV-android-sdk/sdk/native/jni")
set(OpenCV_STATIC OFF)
find_package(OpenCV REQUIRED java)
message(STATUS ${OpenCV_VERSION})

message(STATUS "Configured OpenCV include directories: ${OpenCV_INCLUDE_DIRS}")
include_directories(${OpenCV_INCLUDE_DIRS})

message(STATUS "Configured OpenCV libraries: ${OpenCV_LIBS}")
#####################################################


include_directories(
		${PROJECT_SOURCE_DIR}/src/main/cpp
		${PROJECT_SOURCE_DIR}/src/main/cpp/ORB/include
		${PROJECT_SOURCE_DIR}/src/main/cpp/ORB/
		${PROJECT_SOURCE_DIR}/src/main/cpp/Eigen/
		${PROJECT_SOURCE_DIR}/src/main/cpp/Eigen/src
)


add_library(g2o
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/types_sba.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/types_six_dof_expmap.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/types_sba.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/types_six_dof_expmap.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/types_seven_dof_expmap.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/types_seven_dof_expmap.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/se3quat.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/se3_ops.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/types/se3_ops.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_edge.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_binary_edge.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/hyper_graph_action.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_binary_edge.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/hyper_graph_action.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_multi_edge.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/hyper_graph.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_multi_edge.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/hyper_graph.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_unary_edge.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/linear_solver.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_unary_edge.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/marginal_covariance_cholesky.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_vertex.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/marginal_covariance_cholesky.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/base_vertex.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/matrix_structure.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/batch_stats.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/matrix_structure.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/batch_stats.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/openmp_mutex.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/block_solver.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/block_solver.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/parameter.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/parameter.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/cache.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/cache.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimizable_graph.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimizable_graph.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/solver.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/solver.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/creators.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_factory.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/estimate_propagator.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_factory.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/estimate_propagator.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/factory.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_property.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/factory.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/sparse_block_matrix.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/sparse_optimizer.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/sparse_block_matrix.hpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/sparse_optimizer.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/hyper_dijkstra.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/hyper_dijkstra.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/parameter_container.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/parameter_container.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_with_hessian.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_with_hessian.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_levenberg.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/optimization_algorithm_levenberg.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/jacobian_workspace.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/jacobian_workspace.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/robust_kernel.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/robust_kernel.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/robust_kernel_factory.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/robust_kernel_factory.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/robust_kernel_impl.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/core/robust_kernel_impl.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/string_tools.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/color_macros.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/macros.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/timeutil.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/misc.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/timeutil.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/os_specific.c
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/os_specific.h
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/string_tools.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/property.cpp
		src/main/cpp/ORB/Thirdparty/g2o/g2o/stuff/property.h
)

add_library(DBoW2
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/BowVector.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/FORB.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/FClass.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/FeatureVector.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/ScoringObject.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/TemplatedVocabulary.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DUtils/Random.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DUtils/Timestamp.h
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/BowVector.cpp
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/FORB.cpp
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/FeatureVector.cpp
		src/main/cpp/ORB/Thirdparty/DBoW2/DBoW2/ScoringObject.cpp
		src/main/cpp/ORB/Thirdparty/DBoW2/DUtils/Random.cpp
		src/main/cpp/ORB/Thirdparty/DBoW2/DUtils/Timestamp.cpp
)

add_library(ORBSLAM2
		src/main/cpp/ORB/src/System.cc
		src/main/cpp/ORB/src/Tracking.cc
		src/main/cpp/ORB/src/LocalMapping.cc
		src/main/cpp/ORB/src/LoopClosing.cc
		src/main/cpp/ORB/src/ORBextractor.cc
		src/main/cpp/ORB/src/ORBmatcher.cc
		src/main/cpp/ORB/src/FrameDrawer.cc
		src/main/cpp/ORB/src/Converter.cc
		src/main/cpp/ORB/src/MapPoint.cc
		src/main/cpp/ORB/src/KeyFrame.cc
		src/main/cpp/ORB/src/Map.cc
		src/main/cpp/ORB/src/Optimizer.cc
		src/main/cpp/ORB/src/PnPsolver.cc
		src/main/cpp/ORB/src/Frame.cc
		src/main/cpp/ORB/src/KeyFrameDatabase.cc
		src/main/cpp/ORB/src/Sim3Solver.cc
		src/main/cpp/ORB/src/Initializer.cc
		#src/main/cpp/ORB/src/Viewer.cc
)

add_library(native-lib
		SHARED
		src/main/cpp/native-lib.cpp
		src/main/cpp/Plane.cpp
		src/main/cpp/Frame.cpp
		src/main/cpp/Marker.cpp
		src/main/cpp/Process.cpp
		src/main/cpp/Camera.cpp
)

target_link_libraries(ORBSLAM2
		DBoW2
		g2o
		${EIGEN3_LIBS}
)

target_link_libraries(native-lib
		ORBSLAM2
		${OpenCV_LIBS}
		log
)
