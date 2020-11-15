package app.alarm.ui.list.activity

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.alarm.R
import app.alarm.core.data.db.entities.Alarm
import app.alarm.core.receiver.AlarmHelper
import app.alarm.ui.detail.AlarmDetailActivity
import app.alarm.ui.list.adapter.ListAlarmAdapter
import app.alarm.ui.list.adapter.MyDividerItemDecoration
import app.alarm.utils.AppConstants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ListAlarmFragmentKt : Fragment(), ListAlarmAdapter.OnItemClickListener, View.OnClickListener {
    private var mContext: Activity? = null
    private var mAdapter: ListAlarmAdapter? = null
    private var mLinearLayout: LinearLayout? = null
    private var mAlarmViewModelKt: AlarmViewModelKt? = null

    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        const val TAG = "ListAlarmFragmentKt"
        fun newInstance(): ListAlarmFragmentKt {
            val args = Bundle()
            val fragment = ListAlarmFragmentKt()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity
        EventBus.getDefault().register(this)
        mAlarmViewModelKt = ViewModelProviders.of(this).get(AlarmViewModelKt::class.java)
        getAlarms()
    }

    override fun onDestroyView() {
        job.cancel()
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    @Subscribe
    fun onGetAlarmResult(item: Alarm?) {
        Log.i(TAG, "onGetAlarmResult:data Change item = $item")
        if (item != null) {
            getAlarms()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.list_alarm_fragment, container, false)
        initView(rootView)
        return rootView
    }

    private fun initView(rootView: View) {
        initActionBar()
        mLinearLayout = rootView.findViewById(R.id.layout_no_alarm)
        initRecycleView(rootView)
    }

    private fun setVisibilityNoAlarm(size: Int) {
        if (size == 0) {
            if (mLinearLayout!!.visibility != View.VISIBLE) {
                mLinearLayout!!.visibility = View.VISIBLE
            }
        } else {
            if (mLinearLayout!!.visibility != View.GONE) {
                mLinearLayout!!.visibility = View.GONE
            }
        }
    }

    private fun initActionBar() {
        if (mContext != null && mContext is ListAlarmActivity) {
            val activity = mContext as ListAlarmActivity
            val drawable = ContextCompat.getDrawable(mContext as ListAlarmActivity, R.drawable.ic_plus)
            if (drawable != null) {
                drawable.setColorFilter(ContextCompat.getColor(mContext as ListAlarmActivity, R.color.colorIconCommon), PorterDuff.Mode.SRC_ATOP)
                (mContext as ListAlarmActivity).setImageRightButton(drawable)
            }
            activity.setVisibleLeftButton(View.GONE)
            activity.setButtonListener(this)
            activity.setTittle((mContext as ListAlarmActivity).getString(R.string.alarm_tittle_alarm))
        }
    }

    private fun initRecycleView(rootView: View) {
        val mRecyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)
        mAdapter = ListAlarmAdapter(mContext)
        mAdapter!!.setOnItemClickListener(this)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(mContext)
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.addItemDecoration(MyDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL, 16))
        mRecyclerView.adapter = mAdapter
    }

    private fun getAlarms() {
        uiScope.launch {
            mAlarmViewModelKt!!.getListAlarms()
                    .flowOn(Dispatchers.IO)
                    .collect { alarms: List<Alarm?> ->
                        setVisibilityNoAlarm(alarms.size)
                        mAdapter!!.setAlarms(alarms)
                    }
        }
    }

    override fun onItemClick(alarm: Alarm) {
        Log.i(TAG, "onItemClick: imageItem = $alarm")
        val intent = Intent(mContext, AlarmDetailActivity::class.java)
        intent.putExtra(AppConstants.ARG_INTENT_TYPE_EDIT_ALARM, alarm)
        mContext!!.startActivity(intent)
    }

    override fun onCloseClick(alarm: Alarm, position: Int) {
        Log.i(TAG, "onCloseClick: imageItem = $alarm")
        uiScope.launch {
            mAlarmViewModelKt!!.deleteAlarm(alarm)
                    .flowOn(Dispatchers.IO)
                    .collect { result: Int ->
                        if (result > 0) {
                            mAdapter!!.removeAt(position)
                            val list = mAdapter!!.listAlarms
                            if (list != null && list.size == 0) {
                                setVisibilityNoAlarm(0)
                            }
                            AlarmHelper.enableNextAlert(mContext)
                        }
                    }
        }
    }

    override fun onItemLongClick(alarm: Alarm) {
        Log.i(TAG, "onCloseClick: imageItem = $alarm")
    }

    override fun onSwitchChange(alarm: Alarm, active: Boolean) {
        alarm.active = if (active) Alarm.ALARM_ACTIVE else Alarm.ALARM_INACTIVE
        uiScope.launch {
            mAlarmViewModelKt!!.updateAlarmByActive(alarm.id, alarm.active)
                    .flowOn(Dispatchers.IO)
                    .collect { result: Int ->
                        if (result > 0) {
                            AlarmHelper.enableNextAlert(mContext)
                        }
                    }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.alarm_ab_left_button -> {
            }
            R.id.alarm_ab_right_button -> {
                val intent = Intent(mContext, AlarmDetailActivity::class.java)
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, true)
                mContext!!.startActivity(intent)
            }
            else -> {
            }
        }
    }
}