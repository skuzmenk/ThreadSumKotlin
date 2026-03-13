import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.Random

const val DIM = 100_000_000
lateinit var arr: IntArray

fun main() {
    arr = IntArray(DIM)
    initArr()

    val threadTests = arrayOf(1, 2, 4, 8)

    for (threadNum in threadTests) {
        val startTime = System.currentTimeMillis()

        val result = parallelMin(threadNum)

        val elapsed = System.currentTimeMillis() - startTime

        println("Threads: $threadNum")
        println("Min value: ${result.first}")
        println("Index: ${result.second}")
        println("Elapsed time: $elapsed ms")
    }
}

fun initArr() {
    val rnd = Random()
    for (i in 0 until DIM) {
        arr[i] = rnd.nextInt(1_000_000) + 1
    }
    arr[5_000_000] = -100
}

fun parallelMin(threadNum: Int): Pair<Int, Int> {
    val executor = Executors.newFixedThreadPool(threadNum)
    val part = DIM / threadNum

    val tasks = mutableListOf<Callable<Pair<Int, Int>>>()

    for (i in 0 until threadNum) {
        val start = i * part
        val end = if (i == threadNum - 1) DIM else start + part

        tasks.add(Callable {
            var localMin = arr[start]
            var localIndex = start

            for (j in start until end) {
                if (arr[j] < localMin) {
                    localMin = arr[j]
                    localIndex = j
                }
            }
            Pair(localMin, localIndex)
        })
    }

    val futures = executor.invokeAll(tasks)

    executor.shutdown()

    var globalMin = Int.MAX_VALUE
    var globalIndex = -1

    for (future in futures) {
        val res = future.get()
        if (res.first < globalMin) {
            globalMin = res.first
            globalIndex = res.second
        }
    }

    return Pair(globalMin, globalIndex)
}