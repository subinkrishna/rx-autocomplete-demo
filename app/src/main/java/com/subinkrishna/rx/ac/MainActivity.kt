package com.subinkrishna.rx.ac

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

  private val TAG = "MainActivity"
  private val inputStream = PublishSubject.create<String>()
  private val disposables = CompositeDisposable()

  private val sendEvents = AtomicBoolean(true)
  private lateinit var keywordEdit: EditText
  private val eventsAdapter = EventListAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    keywordEdit = configureInput()
    configureEventList()

    val d = inputStream
        .debounce(300, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .doOnNext {
          runOnUiThread { eventsAdapter.add(it, EventType.Request) }
        }
        .flatMap { keyword ->
          val delay = if (keyword.isNotEmpty())
            1000 + (Math.random() * 1000 * 2).toLong()
          else 0L
          Observable
              .just("${keyword.toUpperCase()} (${delay.toInt()}ms)")
              .delay(delay, TimeUnit.MILLISECONDS)
        }
        .doOnError { runOnUiThread { eventsAdapter.add(it.message ?: it.toString(), EventType.Error) } }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          eventsAdapter.add(it.trim(), EventType.Response)
        }, {})

    disposables.add(d)
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_actions, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == R.id.action_clear) {
      sendEvents.set(false)
      eventsAdapter.clear()
      keywordEdit.setText("")
      sendEvents.set(true)
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun configureInput(): EditText = findViewById<EditText>(R.id.keywordEdit).apply {
    addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val keyword = s?.toString()?.trim() ?: ""
        if (sendEvents.get()) {
          inputStream.onNext(keyword)
        }
      }
    })
  }

  private fun configureEventList(): RecyclerView = findViewById<RecyclerView>(R.id.eventList).apply {
    adapter = eventsAdapter
    layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
    addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
  }
}
