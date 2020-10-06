// EXPECTED_REACHABLE_NODES: 1298
package foo

class MyException(m: String? = null): Exception(m)
class MyException2(m: String? = null): Throwable(m)

class MyException3: Throwable {
    constructor(m: String? = null) : super(m) {}
}

class MyException4(c: Throwable? = null): Throwable(c)
class MyException5: Throwable {
    constructor(c: Throwable? = null) : super(c) {}
}

fun check(e: Throwable, expectedString: String) {
    try {
        throw e
    }
    catch (e: Throwable) {
        assertEquals(expectedString, e.toString())
    }
}

fun box(): String {
    check(Throwable(), "Throwable: null")
    check(Throwable("ccc"), "Throwable: ccc")
    check(Throwable(Throwable("ddd")), "Throwable: Throwable: ddd")
    check(Exception(), "Exception: null")
    check(Exception("bbb"), "Exception: bbb")
    check(Exception(Exception("ccc")), "Exception: Exception: ccc")
    check(AssertionError(), "AssertionError: null")
    check(AssertionError(null), "AssertionError: null")
    check(AssertionError("bbb"), "AssertionError: bbb")
    check(AssertionError(Exception("ccc")), "AssertionError: Exception: ccc")
    check(MyException(), "MyException: null")
    check(MyException("aaa"), "MyException: aaa")
    check(MyException2(), "MyException2: null")
    check(MyException2("aaa"), "MyException2: aaa")
    check(MyException3(), "MyException3: null")
    check(MyException3("aaa"), "MyException3: aaa")

    check(MyException4(), "MyException4")
    check(MyException4(AssertionError()), "MyException4: AssertionError: null")

    check(MyException5(), "MyException5")
    check(MyException5(MyException5()), "MyException5: MyException5")

    return "OK"
}
