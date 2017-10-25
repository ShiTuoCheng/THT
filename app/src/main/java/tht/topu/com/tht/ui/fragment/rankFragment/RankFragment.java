package tht.topu.com.tht.ui.fragment.rankFragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.MainViewPagerAdapter;

public class RankFragment extends Fragment {


    private TabLayout rankTabLayout;
    private ViewPager rankViewPager;
    private RankViewPagerAdapter rankViewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rank, container, false);

        initView(view);
        initData();

        rankViewPager.setAdapter(rankViewPagerAdapter);

        rankViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        rankViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(rankTabLayout));

        rankTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                rankViewPager.setCurrentItem(tab.getPosition());

                int index = tab.getPosition();

                rankTabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));

                if (index == 0){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.selectedweekrank);
                }else if( index == 1){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.selectedmonthrank);
                }else if( index == 2){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.selectedtotalrank);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                rankViewPager.setCurrentItem(tab.getPosition());

                int index = tab.getPosition();

                rankTabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));

                if (index == 0){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.weekrank);
                }else if( index == 1){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.monthrank);
                }else if( index == 2){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.totalrank);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    //初始化视图
    private void initView(View view){

        rankTabLayout = view.findViewById(R.id.rankTabLayout);
        rankViewPager = view.findViewById(R.id.rankViewPager);

    }

    //初始化数据
    private void initData(){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cale;
        String firstday, lastday;
        // 获取前月的第一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        firstday = format.format(cale.getTime());
        // 获取前月的最后一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        lastday = format.format(cale.getTime());
        Log.d("本月第一天和最后一天分别是 ： " , firstday + " and " + lastday);

        Calendar anotherCalendar=Calendar.getInstance();
        anotherCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        anotherCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String monday = format.format(anotherCalendar.getTime());
        anotherCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String sunday = format.format(anotherCalendar.getTime());

        Log.d("当前时间的周一和周末:", monday+" and "+sunday);

        rankViewPagerAdapter = new RankViewPagerAdapter(getChildFragmentManager());

        rankViewPagerAdapter.notifyDataSetChanged();

        rankViewPagerAdapter.addFragment(RankContentFragment.newInstance(monday, sunday));
        rankViewPagerAdapter.addFragment(RankContentFragment.newInstance(firstday, lastday));
        rankViewPagerAdapter.addFragment(RankContentFragment.newInstance("", ""));

        rankViewPager.setOffscreenPageLimit(3);

        rankTabLayout.addTab(rankTabLayout.newTab().setIcon(R.mipmap.selectedweekrank).setText("周排行榜"));
        rankTabLayout.addTab(rankTabLayout.newTab().setIcon(R.mipmap.monthrank).setText("月排行榜"));
        rankTabLayout.addTab(rankTabLayout.newTab().setIcon(R.mipmap.totalrank).setText("总排行榜"));
    }

    private class RankViewPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<Integer> pageIndex = new ArrayList<>();

        public RankViewPagerAdapter(FragmentManager manager, List indexList) {
            super(manager);
            this.pageIndex = indexList;
        }

        private RankViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public int getItemPosition(Object object) {
            // 最简单解决 notifyDataSetChanged() 页面不刷新问题的方法
            return POSITION_NONE;
        }
    }
}
