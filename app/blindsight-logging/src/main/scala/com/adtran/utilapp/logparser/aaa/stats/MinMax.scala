package com.adtran.utilapp.logparser.aaa.stats

import java.time.Instant

/** Helper class to track min/max times of logs, giving the interval over which logs where collected */
case class MinMax(min: Instant, max: Instant)

object MinMax {
  val openRange: MinMax = MinMax(Instant.MIN, Instant.MAX)

  def update(mm: MinMax, i: Instant): MinMax = {

    val m1 = if i.isAfter(mm.min) then mm.copy(min = i) else mm
    val m2 = if i.isBefore(mm.max) then m1.copy(max = i) else m1
    m2
  }
}
