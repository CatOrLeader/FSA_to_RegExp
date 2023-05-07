![Meme](https://user-images.githubusercontent.com/95234842/236702181-740d8ac6-1ad7-4060-8e16-518550a9feef.png)

## _Base Program_: [FSA-Validator](https://github.com/CatOrLeader/FSA-validator)

# FSA-to_RegExp
This is a project with the implementation of FSA to RegExp, which was accepted by the given FSA. 

## **Formats**: 
          --> input: "input.txt"
          --> output: "result.txt" AND console


## **Validation Result**:
| Order | Error |
|------:|-------|
|   1   | E1: Input file is malformed |
|   2   | E2: Initial state is not defined |
|   3   | E3: Set of accepting states is empty |
|   4   | E4: A state 's' is not in the set of states |
|   5   | E5: A transition 'a' is not represented in the alphabet |
|   6   | E6: Some states are disjoint |
|   7   | E7: FSA is nondeterministic |

If the error was occured --> print error message and terminate.

## **Report**:
Regular Expressions, which will be accepted by FSA.

## **Input File Format**: 
states=[s1,s2,...]	  // s1 , s2, ... ∈ latin letters, words and numbers

alpha=[a1,a2, ...]	  // a1 , a2, ... ∈ latin letters, words, numbers and character '_’(underscore)

initial=[s]	          // s ∈ **states**

accepting=[s1,s2,...]	  // s1, s2 ∈ **states**

trans=[s1>a>s2,... ]  // s1,s2,...∈ **states**; a ∈ **alpha**

## **Kleene's Algorithm**
- Denote $\emptyset$ as {};
- Denote Ɛ as eps;
- Define updated rule with the additional parentheses: 

```math
R^k_{ij} = (R^{k-1}_{ik}) (R^{k-1}_{kk}) * (R^{k-1}_{kj}) | (R^{k-1}_{ij})
```

- At each step, each RegExp should be surrounded by parentheses:

```math
R^k_{ij} =((a|eps)(a|eps)*(a|eps)|(a|eps))
```

- The regular expression parts' content should be in lexicographical order, but Ɛ should be at the end of each part:

```math
(a|b|eps)*
```

## **Example**

--> input.txt:
```
states=[0,1] 
alpha=[a,b]
initial=[0]
accepting=[1]
trans=[0>a>0,0>b>1,1>a>1,1>b>1]
```

--> result.txt AND console:
```
(((a|eps)(a|eps)*(b)|(b))(({})(a|eps)*(b)|(a|b|eps))*(({})(a|eps)*(b)|(a|b|eps))|((a|eps)(a|eps)*(b)|(b)))
```
