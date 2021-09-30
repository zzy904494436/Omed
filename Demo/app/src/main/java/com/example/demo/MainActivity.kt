package com.example.demo

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.MainThread
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.SingleSubject
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //创建型

        // interval 定时三秒
        Observable.interval(3, TimeUnit.SECONDS)
            .subscribe(
                {
                    Log.e(TAG, "onCreate: ${it.toInt()} ")
                },
                {
                    Log.e(TAG, "onCreate: ${it.cause.toString()}")
                })

        // SingleSubject
        val singleSubject: SingleSubject<String> = SingleSubject.create()

        // Range
        Observable.range(0, 10).subscribe({
            Log.e(TAG, "onCreate: range ${it}")
        })
        // Repeat
        Observable.range(0, 3).repeat(4).subscribe({
            Log.e(TAG, "onCreate: repeat ${it}")
        })

        //变换型
        // map
        val mapString = "mapChanged"
        Observable.range(0, 10).map(object : Function<Int, String> {
            override fun apply(t: Int): String {
                return "$mapString $t"
            }
        })

        //flatMap 、cast
        val host = "http://www.a.com/"
        val list = arrayListOf("a", "b", "c")
        Observable.fromIterable(list)
            .flatMap(object : Function<String, ObservableSource<out Any>> {
                override fun apply(t: String): ObservableSource<out Any> {
                    return Observable.just("$host$t")
                }
            })
            .cast(String::class.java)
//            .cast(String.Companion.javaClass)
            .subscribe({
                Log.e(TAG, "onCreate: flatMap and cast")
            })

        // concatMap concatMap操作符的功能与flatMap操作符一致，不过它解决了flatMap的交叉问题
        Observable.fromIterable(list)
            .concatMap { Observable.just("$host$it") }
            .cast(String::class.java)
            .subscribe({
                Log.e(TAG, "onCreate: flatMap and cast")
            })

        //flatMapIterable
        Observable.range(0, 10)
            .flatMapIterable {
                listOf(it)
            }
            .subscribe {
                Log.e(TAG, "onCreate: flatMapIterable $it")
            }

        // buffer window
        Observable.just(1, 2, 3, 4, 5, 6, 7)
            .buffer(3)
            .subscribe {

            }

        Observable.just(1, 2, 3, 4, 5, 6, 7)
            .window(3)
            .groupBy {

            }
            .subscribe({

            })

        // groupBy use concat
        val p1 = People("z", "1S")
        val p2 = People("z", "2S")
        val p3 = People("z", "3S")
        val p4 = People("z", "A")
        val p5 = People("z", "1S")
        val p6 = People("z", "A")
        val p7 = People("z", "3S")
        val p8 = People("z", "3S")

        Observable
            .concat(
                Observable.just(p1, p2, p3, p4, p5, p6, p7, p8)
                    .groupBy {
                        it.id
                    })
            .subscribe {
            }

        // 过滤型
        // filter
        Observable.range(0, 10)
            .filter {
                it > 5
            }
            .subscribe {

            }

        // elementAt
        Observable.range(0, 100).elementAt(55)
            .subscribe {

            }
        // distinct
        Observable.just(2, 3, 2, 2, 3, 1, 4)
            .distinct()
            .subscribe {

            }

        // skip take skiplast takelast
        Observable.range(0, 20)
            .skip(5)
            .subscribe {
                Log.e(TAG, "onCreate:skip  $it")
            }

        Observable.range(0, 20)
            .take(5)
            .subscribe {
                Log.e(TAG, "onCreate:take  $it")
            }

        Observable.range(0, 20)
            .skipLast(5)
            .subscribe {
                Log.e(TAG, "onCreate:skiplast  $it")
            }

        Observable.range(0, 20)
            .skipLast(5)
            .subscribe {
                Log.e(TAG, "onCreate:takelast  $it")
            }

        // ignoreElements
        Observable.range(0, 10)
            .ignoreElements()
            .subscribe {
                // onComplete
            }

        // throttleFirst throttleLast throttleLasted
        Observable.create<Int> {
            for (i in 0..9) {
                it.onNext(i)
                Thread.sleep(100)
            }
            it.onComplete()
        }
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.e(TAG, "onCreate: throttleFirst $it")
            }

        // throttleWithTimeOut
        Observable.create<Int> {
            for (i in 0..9) {
                it.onNext(i)
                var sleep: Long = 100;
                if (i % 3 == 0) {
                    sleep = 300
                }
                Thread.sleep(sleep)
            }
            it.onComplete()
        }
            .throttleWithTimeout(200, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.e(TAG, "onCreate: throttleWithTimeout $it")
            }

        // 组合型
        // merge 可能会让合并的Observable发射的数据交错
        val o1: Observable<Int> = Observable.just(1, 2, 3).subscribeOn(Schedulers.io())
        val o2: Observable<Int> = Observable.just(4, 5, 6)
        Observable.merge(o1, o2)
            .subscribe {
                Log.e(TAG, "onCreate: merge $it")
            }
        // concat 有先后顺序
        Observable.concat(o1, o2)
            .subscribe {
                Log.e(TAG, "onCreate: concat $it")
            }

        // zip 组合 + 变换 zip操作如用于最近未打包的两个Observable，只有当原始Observable中的每一个都发射了一条数据时zip才发射数据
        val o3: Observable<Int> = Observable.just(1, 2, 3)
        val o4: Observable<String> = Observable.just("4", "5", "6")
        Observable.zip(o3, o4, { i: Int, s: String -> "$s$i" })
            .subscribe {
                Log.e(TAG, "onCreate: zip $it")
            }

        // combineLatest 用于最近发射的数据项，在原始的Observable中的任意一个发射了数据时侯继续发射一条数据
        Observable.combineLatest(o3, o4, { int: Int, string: String -> "$string $int" })
            .subscribe {
                Log.e(TAG, "onCreate: combineLatest $it")
            }

        // 辅助型
        // delay
        Observable.create<Long> {
            val time = System.currentTimeMillis();
            it.onNext(time)
        }
            .delay(3,TimeUnit.SECONDS)
            .subscribe {
                val offset = System.currentTimeMillis() - it
                Log.e(TAG, "onCreate: delay $offset"  )
            }
        // do
        Observable.just(1)
            .doOnSubscribe { }
            .doOnNext { }
            .doAfterNext { }
            .doOnError { }
            .doOnDispose { }
            .doOnComplete { }
            .doFinally { }
            .doOnTerminate { }
            .doAfterTerminate { }
            .doOnEach { }
            .subscribe({

            }, {

            }, {

            })

        // subscribeOn observeOn

        // timeout

        // 错误操作
        // catch
        // retry

        // 布尔操作符
        // all
        // contains
        // isEmpty

        // 条件
        // amb
        // defaultIfEmpty

        // 转换
        // toList
        // toSortedList
        // toMap

    }
}

// example
class People(var name: String, var id: String)