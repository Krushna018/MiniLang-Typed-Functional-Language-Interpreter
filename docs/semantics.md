# MiniLang typing and operational-semantics notes

MiniLang is a deliberately small language used to study the relationship between static typing and evaluation. The implementation is executable; this document records the corresponding research-style specification.

## Core syntax

The implementation includes more than twelve expression forms, including integer, Boolean, string, and unit literals; variables; `let`; recursive `let`; conditionals; typed lambdas; explicit type abstraction/application; function application; pairs and projections; unary operators; and binary arithmetic, comparison, equality, Boolean, and string-concatenation operators.

## Selected typing judgments

Write `Γ ⊢ e : τ` for “under environment Γ, expression e has type τ”.

### Variables

If `x : τ ∈ Γ`, then:

```
Γ ⊢ x : τ
```

### Functions

```
Γ, x : τ1 ⊢ e : τ2
-------------------------
Γ ⊢ fun(x:τ1):τ2 => e : τ1 -> τ2
```

### Application

```
Γ ⊢ e1 : τ1 -> τ2    Γ ⊢ e2 : τ1
---------------------------------
          Γ ⊢ e1(e2) : τ2
```

### Conditionals

```
Γ ⊢ c : Bool    Γ ⊢ t : τ    Γ ⊢ f : τ
---------------------------------------
        Γ ⊢ if c then t else f : τ
```

### Pairs

```
Γ ⊢ e1 : τ1    Γ ⊢ e2 : τ2
---------------------------
Γ ⊢ (e1, e2) : (τ1, τ2)
```

### Explicit parametric polymorphism

MiniLang uses explicit type abstraction rather than Hindley–Milner inference. A term such as:

```
fun[T](x: T): T => x
```

has type:

```
forall T. T -> T
```

and `id[Int]` substitutes `Int` for `T` before ordinary function application is checked.

## Small-step intuition

The executable evaluator is environment-based and call-by-value. A corresponding small-step presentation can be defined with evaluation contexts. Representative rules include:

```
if true then e1 else e2  --> e1
if false then e1 else e2 --> e2
```

and, for a value `v`:

```
(fun(x:τ):σ => e)(v) --> e[x := v]
```

The interpreter uses closures rather than syntactic substitution, but the observable behavior corresponds to this substitution-based rule for closed programs.

## Progress / preservation reasoning

For the core simply typed subset, the project documents the standard safety argument:

- **Progress:** a closed, well-typed expression is either a value or can take an evaluation step.
- **Preservation:** if `Γ ⊢ e : τ` and `e --> e'`, then `Γ ⊢ e' : τ`.

The automated suite complements (but does not replace) the proof argument by exercising both well-typed and intentionally ill-typed programs across scoping, recursion, higher-order functions, pairs, Boolean logic, and explicit polymorphism.

## Research limitations

This is an educational/research prototype, not a production compiler. It intentionally omits mutation, subtyping, effects, ownership/borrowing, higher-kinded types, asynchronous semantics, and full mechanized proofs. Those omissions create natural directions for future work.
