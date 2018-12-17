package com.xgx.musicplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.TimeUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xgx on 2018/12/13 for MusicPlayDemo
 */
public class HistoryListActivity extends Activity implements CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener {
    @BindView(R.id.titlebar)
    CommonTitleBar titlebar;
    @BindView(R.id.lv)
    RecyclerView lv;
    @BindView(R.id.numTv)
    TextView numTv;
    @BindView(R.id.calendarView)
    CalendarView calendarView;
    @BindView(R.id.calendarLayout)
    CalendarLayout calendarLayout;
    private List<Music> data;
    private MusicHistoryListAdapter mAdapter;
    private long delayMillis = 500;
    private long TOTAL_COUNTER;
    private int mCurrentCounter;
    private boolean isErr = true;
    private int page = 0;
    private ImageView selectedTime;
    private ImageView selectedDelete;
    private int mYear;
    private String currentTime;
    private View notDataView;
    private View selectedExpand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        titlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == titlebar.ACTION_LEFT_TEXT || action == titlebar.ACTION_LEFT_BUTTON) {
                    finish();
                }
            }

        });
        mYear = calendarView.getCurYear();
        titlebar.getCenterSubTextView().setText(mYear + "年");
        selectedTime = titlebar.getRightCustomView().findViewById(R.id.selected_time);
        selectedDelete = titlebar.getRightCustomView().findViewById(R.id.selected_delete);
        selectedExpand = titlebar.getRightCustomView().findViewById(R.id.selected_expand);
        selectedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!calendarLayout.isExpand()) {
                    calendarView.showYearSelectLayout(mYear);
                    return;
                }
                calendarView.showYearSelectLayout(mYear);
            }
        });
        selectedExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (View.VISIBLE == calendarView.getVisibility()) {
                    calendarView.setVisibility(View.INVISIBLE);
                } else {
                    calendarView.setVisibility(View.VISIBLE);
                }
            }
        });
        calendarView.setOnYearChangeListener(this);
        calendarView.setOnCalendarSelectListener(this);
        selectedDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(HistoryListActivity.this);
                builder.setTitle("请注意");
                builder.setMessage("是否要清空历史记录？");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MyApplication.getDaoInstant().getMusicDao().deleteAll();
                        page = 0;
                        getData();
                    }
                });
                builder.show();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 设置布局管理器
        lv.setLayoutManager(layoutManager);
        mAdapter = new MusicHistoryListAdapter(data);
        mAdapter.openLoadAnimation();
        notDataView = getLayoutInflater().inflate(R.layout.empty_layout, (ViewGroup) lv.getParent(), false);
        mAdapter.setEmptyView(notDataView);
        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                lv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentCounter >= TOTAL_COUNTER) {
                            //数据全部加载完毕
                            mAdapter.loadMoreEnd();
                        } else {
                            if (isErr) {
                                //成功获取更多数据
                                getData();

                                mCurrentCounter = mAdapter.getData().size();
                                mAdapter.loadMoreComplete();
                            } else {
                                //获取更多数据失败
                                isErr = true;
                                mAdapter.loadMoreFail();

                            }
                        }
                    }

                }, delayMillis);
            }
        }, lv);
        lv.setAdapter(mAdapter);
        page = 0;
        currentTime = calendarView.getCurYear() + "-" + calendarView.getCurMonth() + "-" + calendarView.getCurDay();
        getData();
    }

    public void changeNum() {
        numTv.setText("#" + currentTime + " 查询到" + mAdapter.getData().size() + "条记录");

    }

    private void getData() {
        //根据page分页
        MusicDao dao = MyApplication.getDaoInstant().getMusicDao();
        QueryBuilder builder = dao.queryBuilder();
        TOTAL_COUNTER = builder.count();
        List<Music> listMsg = builder.where(MusicDao.Properties.Scantime.like("%" + currentTime + "%")).orderDesc(MusicDao.Properties.Id).offset(page * 10).limit(10).list();
        if (page == 0) {
            mAdapter.setNewData(listMsg);
        } else {
            mAdapter.addData(listMsg);
        }
        page++;
        numTv.setText("#" + currentTime + " 查询到" + mAdapter.getData().size() + "条记录");

    }

    @Override
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        currentTime = calendar.getYear() + "-" + calendar.getMonth() + "-" + calendar.getDay();
        page = 0;
        getData();

        mYear = calendar.getYear();

    }

    @Override
    public void onYearChange(int year) {
        currentTime = calendarView.getCurYear() + "-" + calendarView.getCurMonth() + "-" + calendarView.getCurDay();
        mYear = year;
        titlebar.getCenterSubTextView().setText(year + "年");
        page = 0;
        getData();
        numTv.setText("#" + currentTime + " 查询到" + mAdapter.getItemCount() + "条记录");

    }


}
