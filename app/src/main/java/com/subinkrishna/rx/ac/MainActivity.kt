package com.subinkrishna.rx.ac

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

  private val TAG = "MainActivity"
  private val inputStream = PublishSubject.create<String>()
  private val disposables = CompositeDisposable()

  private val eventsAdapter = EventListAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val keywordEdit = configureInput()
    val eventList = configureEventList()

    val d = inputStream
        //.debounce(300, TimeUnit.MILLISECONDS)
        //.filter { it.length > 2 }
        //.distinctUntilChanged()
        .doOnNext { eventsAdapter.add(it, EventType.Request) }
        .flatMap { keyword ->
          val delay = Math.random() * 1000
          Observable
              .just("${keyword.toUpperCase()} (${delay.toInt()}ms)")
              .delay(delay.toLong(), TimeUnit.MILLISECONDS)
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          eventsAdapter.add(it, EventType.Response)
          eventList.scrollToPosition(eventsAdapter.itemCount)
        }, {})
    disposables.add(d)
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  private fun configureInput(): EditText = findViewById<EditText>(R.id.keywordEdit).apply {
    addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val keyword = s?.toString()?.trim() ?: ""
        inputStream.onNext(keyword)
      }
    })
  }

  private fun configureEventList(): RecyclerView = findViewById<RecyclerView>(R.id.eventList).apply {
    adapter = eventsAdapter
    layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
    addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
  }
}
