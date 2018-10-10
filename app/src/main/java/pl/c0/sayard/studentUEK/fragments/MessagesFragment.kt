package pl.c0.sayard.studentUEK.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import pl.c0.sayard.studentUEK.BackButtonEditText
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.adapters.MessagesAdapter
import pl.c0.sayard.studentUEK.data.Message
import pl.c0.sayard.studentUEK.db.DatabaseManager


class MessagesFragment : Fragment() {

    private var messagesSearch:BackButtonEditText? = null

    companion object {
        fun newInstance(): MessagesFragment{
            return MessagesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        val errorMessage = view.findViewById<TextView>(R.id.messages_message)
        val progressBar = view.findViewById<ProgressBar>(R.id.messages_progress_bar)
        val messagesSwipe = view.findViewById<SwipeRefreshLayout>(R.id.messages_swipe)
        progressBar.visibility = View.VISIBLE

        val dbManager = DatabaseManager(context!!)
        val messages = dbManager.getMessagesFromDb()
        if(messages.isEmpty()){
            errorMessage.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }else{
            errorMessage.visibility = View.GONE
            val adapter = getAdapter(messages)
            messagesSearch = view.findViewById(R.id.messages_search)
            messagesSearch?.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    adapter.filter.filter(s.toString())
                }
            })
            messagesSearch?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(!hasFocus && messagesSearch?.text.toString() == ""){
                    messagesSearch?.visibility = View.GONE
                }
            }

            val listView = view.findViewById<ListView>(R.id.messages_list_view)
            listView.adapter = adapter
            listView.setOnScrollListener(object: AbsListView.OnScrollListener{
                override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    var topRowVerticalPosition = 0
                    if(listView != null && listView.childCount != 0){
                        topRowVerticalPosition = listView.getChildAt(0).top
                    }
                    messagesSwipe.isEnabled = (firstVisibleItem == 0 && topRowVerticalPosition >= 0)
                }

                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                }

            })
            progressBar.visibility = View.GONE
        }


        messagesSwipe.setOnRefreshListener {
            val ft = activity?.supportFragmentManager?.beginTransaction()
            ft?.detach(this)
            ft?.attach(this)
            ft?.commit()
            messagesSwipe.isRefreshing = false
        }

        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.messages_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.search_message ->{
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if(messagesSearch?.visibility == View.GONE){
                    messagesSearch?.visibility = View.VISIBLE
                    messagesSearch?.isFocusableInTouchMode = true
                    messagesSearch?.requestFocus()
                    imm.showSoftInput(messagesSearch, InputMethodManager.SHOW_IMPLICIT)
                }else{
                    messagesSearch?.visibility = View.GONE
                    imm.hideSoftInputFromWindow(messagesSearch?.windowToken, 0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAdapter(messages: List<Message>): MessagesAdapter {
        return MessagesAdapter(context!!, activity, this, messages.toMutableList())
    }

}
