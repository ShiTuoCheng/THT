package tht.topu.com.tht.ui.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.haha.perflib.Main;

import java.util.ArrayList;
import java.util.List;

import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.MainViewPagerAdapter;
import tht.topu.com.tht.ui.fragment.rankFragment.RankFragment;
import tht.topu.com.tht.ui.fragment.userForumBlogFragment.UserForumBlogFragment;
import tht.topu.com.tht.ui.fragment.userForumBlogFragment.UserForumReviewsFragment;

public class UserForumBlogActivity extends AppCompatActivity {

    private TabLayout rankTabLayout;
    private ViewPager rankViewPager;
    private UserBlogViewPagerAdapter userBlogViewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_forum_blog);

        initView();

        userBlogViewPagerAdapter = new UserBlogViewPagerAdapter(getSupportFragmentManager());

        userBlogViewPagerAdapter.addFragment(UserForumBlogFragment.newInstance(true));
        userBlogViewPagerAdapter.addFragment(new UserForumReviewsFragment());

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

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.selected_blog);
                }else if( index == 1){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.selected_favorite);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                rankViewPager.setCurrentItem(tab.getPosition());

                int index = tab.getPosition();

                rankTabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));

                if (index == 0){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.blog);
                }else if( index == 1){

                    rankTabLayout.getTabAt(tab.getPosition()).setIcon(R.mipmap.favorite);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        rankViewPager.setOffscreenPageLimit(3);
        rankTabLayout.addTab(rankTabLayout.newTab().setIcon(R.mipmap.selectedweekrank).setText("我的帖子"));
        rankTabLayout.addTab(rankTabLayout.newTab().setIcon(R.mipmap.favorite).setText("我的评论"));

        rankViewPager.setAdapter(userBlogViewPagerAdapter);

    }

    //初始化视图
    private void initView(){

        rankTabLayout = (TabLayout)findViewById(R.id.userBlogTabLayout);
        rankViewPager = (ViewPager)findViewById(R.id.userBlogViewPager);
    }

    private class UserBlogViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<Integer> pageIndex = new ArrayList<>();

        public UserBlogViewPagerAdapter(FragmentManager manager, List indexList) {
            super(manager);
            this.pageIndex = indexList;
        }

        private UserBlogViewPagerAdapter(FragmentManager manager) {
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
