package com.ashehata.sofra.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.ashehata.sofra.R;
import com.ashehata.sofra.data.local.room.AppDatabase;
import com.ashehata.sofra.data.local.shared.SharedPreferencesManger;
import com.ashehata.sofra.data.model.reataurant.foodItem.FoodItemData;
import com.ashehata.sofra.ui.fragment.client.FoodItemsFragment;
import com.ashehata.sofra.ui.fragment.client.RestaurantsFragment;
import com.ashehata.sofra.ui.fragment.restaurant.CategoriesFragment;
import com.ashehata.sofra.ui.fragment.restaurant.MoreFragment;
import com.ashehata.sofra.ui.fragment.restaurant.OrderFragment;
import com.ashehata.sofra.ui.fragment.restaurant.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ashehata.sofra.data.local.shared.SharedPreferencesManger.TYPE_CLIENT;
import static com.ashehata.sofra.data.local.shared.SharedPreferencesManger.TYPE_RESTAURANT;
import static com.ashehata.sofra.helper.HelperMethod.ReplaceFragment;
import static com.ashehata.sofra.helper.HelperMethod.onPermission;

public class HomeActivity extends BaseActivity {

    // Restaurant fragments
    MoreFragment moreFragment;
    CategoriesFragment categoriesFragment;
    ProfileFragment profileFragment;
    OrderFragment orderFragment;
    // Client fragments
    RestaurantsFragment restaurantsFragment;

    String userType = "";


    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            switch (itemId) {
                case R.id.navigation_home:
                   homeFragment();
                    break;

                case R.id.navigation_clipboard:
                    ReplaceFragment(getSupportFragmentManager(), orderFragment
                            , R.id.home_activity_fl_display, false);
                    break;

                case R.id.navigation_user:
                    ReplaceFragment(getSupportFragmentManager(), profileFragment
                            , R.id.home_activity_fl_display, false);
                    break;

                case R.id.navigation_dots:
                    ReplaceFragment(getSupportFragmentManager(), moreFragment
                            , R.id.home_activity_fl_display, false);
                    break;
            }
            return true;
        }
    };

    @BindView(R.id.home_activity_fl_notification)
    FrameLayout homeActivityFlNotification;
    @BindView(R.id.home_activity_fl_calc)
    FrameLayout homeActivityFlCalc;
    @BindView(R.id.home_activity_iv_ic_shopping)
    ImageView homeActivityIvIcShopping;
    @BindView(R.id.home_activity_tv_food_items_num)
    public TextView homeActivityTvFoodItemsNum;
    FoodItemsFragment foodItemsFragment = new FoodItemsFragment() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setHomeFragments();
        //get user type
        userType = SharedPreferencesManger.LoadUserType(HomeActivity.this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        navView.setSelectedItemId(R.id.navigation_home);

        //grant permission
        onPermission(this);
        Log.v("user_type", userType);
        //set shopping icon in ( client )
        changeIcon();

    }

    private void changeIcon() {
        if (userType.equals(TYPE_CLIENT)) {
            homeActivityIvIcShopping.setImageResource(R.drawable.ic_shopping_cart_solid);
        }
    }

    private void setHomeFragments() {
        moreFragment = new MoreFragment();
        categoriesFragment = new CategoriesFragment();
        profileFragment = new ProfileFragment();
        orderFragment = new OrderFragment();
        restaurantsFragment = new RestaurantsFragment();
    }

    @OnClick({R.id.home_activity_fl_notification, R.id.home_activity_fl_calc})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.home_activity_fl_notification:
                break;
            case R.id.home_activity_fl_calc:
                doAction();
                break;
        }
    }

    private void doAction() {
        if (userType.equals(TYPE_RESTAURANT)) {

        } else if (userType.equals(TYPE_CLIENT)) {
            ReplaceFragment(getSupportFragmentManager(), foodItemsFragment
                    , R.id.home_activity_fl_display, true);
        }
    }

    private void homeFragment() {

        if (userType.equals(TYPE_RESTAURANT)) {
            ReplaceFragment(getSupportFragmentManager(), categoriesFragment
                    , R.id.home_activity_fl_display, false);
            homeActivityTvFoodItemsNum.setVisibility(View.GONE);


        } else if (userType.equals(TYPE_CLIENT)) {
            ReplaceFragment(getSupportFragmentManager(), restaurantsFragment
                    , R.id.home_activity_fl_display, false);
            homeActivityTvFoodItemsNum.setVisibility(View.VISIBLE);
            getFoodItemsNumber();
        }
    }

    private void getFoodItemsNumber() {
        AppDatabase.getInstance(this).userDao().getAllFoodItems().observe(this, new Observer<List<FoodItemData>>() {
            @Override
            public void onChanged(List<FoodItemData> foodItemData) {

                //set items number
                homeActivityTvFoodItemsNum.setText(foodItemData.size()+"");
            }
        });
    }
}
