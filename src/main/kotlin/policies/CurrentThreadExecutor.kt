package policies

import java.util.concurrent.Executor

// https://stackoverflow.com/a/6583868/5374021
class CurrentThreadExecutor : Executor {
    override fun execute(r: Runnable) {
        r.run()
    }
}