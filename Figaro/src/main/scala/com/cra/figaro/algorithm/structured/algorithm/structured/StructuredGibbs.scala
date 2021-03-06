/*
 * StructuredGibbs.scala
 * A structured Gibbs sampling algorithm.
 *
 * Created By:      William Kretschmer (kretsch@mit.edu)
 * Creation Date:   Aug 7, 2015
 *
 * Copyright 2017 Avrom J. Pfeffer and Charles River Analytics, Inc.
 * See http://www.cra.com or email figaro@cra.com for information.
 *
 * See http://www.github.com/p2t2/figaro for a copy of the software license.
 */
package com.cra.figaro.algorithm.structured.algorithm.structured

import com.cra.figaro.algorithm.structured.solver._
import com.cra.figaro.algorithm.structured.strategy.solve._
import com.cra.figaro.language._
import com.cra.figaro.algorithm.structured.algorithm._
import com.cra.figaro.algorithm.factored.gibbs.Gibbs
import com.cra.figaro.algorithm.factored.gibbs.BlockSampler

class StructuredGibbs(universe: Universe, numSamples: Int, burnIn: Int, interval: Int, blockToSampler: Gibbs.BlockSamplerCreator, targets: Element[_]*)
  extends StructuredProbQueryAlgorithm(universe, targets: _*) with DecompositionProbQuery {

  def solvingStrategy() = new ConstantStrategy(problem, structuredRaising, marginalGibbs(numSamples, burnIn, interval, blockToSampler))
}

object StructuredGibbs {
  /**
   * Create a structured Gibbs algorithm.
   */
  def apply(numSamples: Int, targets: Element[_]*) = {
    if (targets.isEmpty) throw new IllegalArgumentException("Cannot run Gibbs with no targets")
    val universes = targets.map(_.universe).toSet
    if (universes.size > 1) throw new IllegalArgumentException("Cannot have targets in different universes")
    new StructuredGibbs(targets(0).universe, numSamples, 0, 1, BlockSampler.default, targets:_*)
  }

  /**
   * Create a structured Gibbs algorithm.
   */
  def apply(numSamples: Int, burnIn: Int, interval: Int, blockToSampler: Gibbs.BlockSamplerCreator, targets: Element[_]*) = {
    if (targets.isEmpty) throw new IllegalArgumentException("Cannot run Gibbs with no targets")
    val universes = targets.map(_.universe).toSet
    if (universes.size > 1) throw new IllegalArgumentException("Cannot have targets in different universes")
    new StructuredGibbs(targets(0).universe, numSamples, burnIn, interval, blockToSampler, targets:_*)
  }

  /**
   * Use Gibbs to compute the probability that the given element satisfies the given predicate.
   */
  def probability[T](target: Element[T], predicate: T => Boolean): Double = {
    val alg = this(10000, target)
    alg.start()
    val result = alg.probability(target, predicate)
    alg.kill()
    result
  }

  /**
   * Use Gibbs to compute the probability that the given element has the given value.
   */
  def probability[T](target: Element[T], value: T): Double =
    probability(target, (t: T) => t == value)
}
