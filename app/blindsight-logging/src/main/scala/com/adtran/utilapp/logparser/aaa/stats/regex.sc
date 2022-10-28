val regex = "=(.+?)(/|$)".r
val d1    = "http://foo.com/some/path/value=123moan"
val data  = "http://foo.com/some/path/value=123moan/child/name=foo"
val data  = "http://foo.com/some/path/value=123moan/child/name=foo"

val r = regex.unanchored

val m1 = r.findAllIn(d1)
println(m1)
r.replaceAllIn(data, "=XX/")
