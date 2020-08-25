# KDD20-DLM

Code release of ["Imputing Various Incomplete Attributes vid Distance Likelihood Maximization" (KDD 20)](https://dl.acm.org/doi/10.1145/3394486.3403096).

Parameters
----------
The input and output of **DLM** algorithm is:

Method

```
setParams(K, L, Can);
```

Input:

```
int K;  // the number of considered largest likelihoods 
int L;  // the number of learning neighbors for distance models
int Can;  // the number of candidates for imputation
```

Output

```
HashMap<Position, Cell> cellMap
```

Library
----------
[jama.jar](http://math.nist.gov/javanumerics/jama/) is used to implement the code.

Citation
----------
If you use this code for your research, please consider citing:

```
@inproceedings{DBLP:conf/kdd/SongS20,
  author    = {Shaoxu Song and
               Yu Sun},
  title     = {Imputing Various Incomplete Attributes via Distance Likelihood Maximization},
  booktitle = {{SIGKDD}},
  pages     = {535--545},
  publisher = {{ACM}},
  year      = {2020}
}
```