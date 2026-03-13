import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.Random
// Якщо програма падає з помилкою OutOfMemoryError,
// зменште це значення (наприклад, до 10_000_000) або збільште пам'ять JVM (-Xmx2G)
const val DIM = 100_000_000
lateinit var arr: IntArray

fun main() {
    // 1. Ініціалізація
    arr = IntArray(DIM)
    initArr()

    val threadTests = arrayOf(1, 2, 4, 8)

    // 2. Тестування для різної кількості потоків
    for (threadNum in threadTests) {
        val startTime = System.currentTimeMillis()

        val result = parallelMin(threadNum)

        val elapsed = System.currentTimeMillis() - startTime

        println("Threads: $threadNum")
        println("Min value: ${result.first}")
        println("Index: ${result.second}")
        println("Elapsed time: $elapsed ms")
        println("-----------------------------")
    }
}

fun initArr() {
    val rnd = Random()
    for (i in 0 until DIM) {
        arr[i] = rnd.nextInt(1_000_000) + 1
    }
    arr[5_000_000] = -100 // Штучний мінімум
}

fun parallelMin(threadNum: Int): Pair<Int, Int> {
    // Створюємо пул потоків
    val executor = Executors.newFixedThreadPool(threadNum)
    val part = DIM / threadNum

    // Список задач (Callable), кожна з яких повертатиме Pair(min, index)
    val tasks = mutableListOf<Callable<Pair<Int, Int>>>()

    for (i in 0 until threadNum) {
        val start = i * part
        val end = if (i == threadNum - 1) DIM else start + part

        // Додаємо задачу в список
        tasks.add(Callable {
            var localMin = arr[start]
            var localIndex = start

            for (j in start until end) {
                if (arr[j] < localMin) {
                    localMin = arr[j]
                    localIndex = j
                }
            }
            // Задача просто повертає результат, ніяких спільних масивів
            Pair(localMin, localIndex)
        })
    }

    // Запускаємо всі задачі паралельно і чекаємо на їх виконання
    val futures = executor.invokeAll(tasks)

    // Обов'язково закриваємо пул потоків після використання
    executor.shutdown()

    // Знаходимо абсолютний мінімум серед результатів усіх потоків
    var globalMin = Int.MAX_VALUE
    var globalIndex = -1

    for (future in futures) {
        // .get() дістає результат роботи Callable (зачекавши, якщо треба)
        val res = future.get()
        if (res.first < globalMin) {
            globalMin = res.first
            globalIndex = res.second
        }
    }

    return Pair(globalMin, globalIndex)
}