# LiSA IOTJoyCar 

Running a taint analysis to detect possible cross-language injection vulnerabilites on a sample C++ and Java program using [LiSA](https://github.com/lisa-analyzer/lisa).

The original code ([this folder](/original/) contain simplified versions of those sources), used as running example in [this paper](http://dx.doi.org/10.14279/tuj.eceasst.77.1104), is taken from [this repository](https://github.com/amitmandalnitdgp/IOTJoyCar). Specifically, the sources are missing debug statements (i.e. printing to console) and C++ functions used to instruct CodeSonar about sources, sinks and sanitizers.

To execute, use `./gradlew run`. As described in the paper, the analysis can generate zero or one warning, depending if you consider function `map` as a sanitizer. Execute `./gradlew run --args="sanitize"` to consider it. The program will build the CFG representation and execute LiSA inside `analysis/<random UUID>`, dumping a json report and the dot files with the analysis results (where `_|_` represents a bottom value, `_` represents a clean value and `#` represents a tainted value). The warnings generated, as well as the folder containing the results, will be shown at the end of the log.

Version of LiSA used is `0.1b8`.
