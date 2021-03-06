package com.ashehata.sofra.adapter.restaurant;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashehata.sofra.R;
import com.ashehata.sofra.data.api.GetDataService;
import com.ashehata.sofra.data.api.RetrofitClient;
import com.ashehata.sofra.data.local.room.AppDatabase;
import com.ashehata.sofra.data.local.shared.SharedPreferencesManger;
import com.ashehata.sofra.data.model.reataurant.foodItem.FoodItem;
import com.ashehata.sofra.data.model.reataurant.foodItem.FoodItemData;
import com.ashehata.sofra.helper.InternetState;
import com.ashehata.sofra.ui.activity.BaseActivity;
import com.ashehata.sofra.ui.fragment.client.OrderFoodFragment;
import com.ashehata.sofra.ui.fragment.restaurant.AddFoodFragment;

import java.util.List;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ashehata.sofra.helper.HelperMethod.ReplaceFragment;
import static com.ashehata.sofra.helper.HelperMethod.createToast;
import static com.ashehata.sofra.helper.HelperMethod.dismissProgressDialog;
import static com.ashehata.sofra.helper.HelperMethod.onLoadImageFromUrl;
import static com.ashehata.sofra.helper.HelperMethod.showProgressDialog;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder>  {

    private View.OnClickListener onClickListener;
    private Context context;
    private BaseActivity activity;
    private List<FoodItemData> foodItemData;
    private FragmentManager fragmentManager;
    private String mToken;
    private GetDataService getDataService;
    private int customView ;
    private int currentQuantity;



    public FoodItemAdapter(Activity activity, Context context, List<FoodItemData> foodItemData,int customView) {
        this.activity = (BaseActivity) activity;
        this.context = context;
        this.foodItemData = foodItemData;
        mToken =  SharedPreferencesManger.LoadData(activity,SharedPreferencesManger.API_TOKEN_RESTAURANT);
        getDataService = RetrofitClient.getClient().create(GetDataService.class);
        this.customView = customView;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(customView,
                parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FoodItemAdapter.ViewHolder holder, int position) {
        setData(holder, position);

        if(customView==R.layout.custom_food_item){
            setActionForRestaurant(holder, position);
        }else if(customView==R.layout.custom_restaurant_item) {
            setActionForClient(holder,position);
        }else {
            setActionForFoodItemDb(holder,position);
        }
    }

    private void setActionForFoodItemDb(ViewHolder holder, int position) {
        int itemId = foodItemData.get(position).get_id();
        holder.deleteFoodItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog(activity,context.getString(R.string.wait_moment));
                deleteFoodItemFromDb(itemId,foodItemData.get(position));
            }
        });


        holder.increaseFoodItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseQuantity(itemId);
            }
        });


        holder.decreaseFoodItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseQuantity(itemId);
            }
        });

    }
    private void decreaseQuantity(int itemId) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                currentQuantity =AppDatabase.getInstance(context).userDao().getFoodItem(itemId).getQuantity();
                currentQuantity--;
                // set min value of quantity = 1
                if (currentQuantity== 0){
                    currentQuantity = 1;
                }
                // set new  value of quantity
                setItemQuantity(itemId,currentQuantity);
            }
        });
    }

    private void increaseQuantity(int itemId) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                currentQuantity =AppDatabase.getInstance(context).userDao().getFoodItem(itemId).getQuantity();
                currentQuantity++;
                // set new  value of quantity
                setItemQuantity(itemId,currentQuantity);
            }
        });
    }

    private void setItemQuantity(int itemId,int newQuantity) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(context).userDao().updateFoodItemQuantity(itemId,newQuantity);
            }
        });
    }

    private void deleteFoodItemFromDb(int itemId,FoodItemData foodItemData) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //AppDatabase.getInstance(context).userDao().deleteFoodItem(itemId);
                AppDatabase.getInstance(context).userDao().deleteFoodItem(foodItemData);

                dismissProgressDialog();
            }
        });
    }

    private void setActionForClient(ViewHolder holder, int position) {
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderFoodFragment orderFoodFragment = new OrderFoodFragment();
                orderFoodFragment.foodItemData = foodItemData.get(position);
                ReplaceFragment(activity.getSupportFragmentManager(),orderFoodFragment
                        , R.id.home_activity_fl_display, true);
            }
        });
    }

    private void setActionForRestaurant(ViewHolder holder, int position) {

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFoodItem(holder, position);
            }
        });

        holder.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFoodFragment addFoodFragment= new AddFoodFragment();
                addFoodFragment.isUpdate = true;
                addFoodFragment.foodItemData = foodItemData.get(position);
                ReplaceFragment(activity.getSupportFragmentManager(),addFoodFragment
                        , R.id.home_activity_fl_display, true);
            }
        });
    }

    private void deleteFoodItem(ViewHolder holder, int position) {

        if(mToken!=null){
            if(InternetState.isConnected(context)){
                showProgressDialog(activity, activity.getString(R.string.wait_moment));
                getDataService.deleteFoodItem(mToken,foodItemData.get(position).getId()+"").enqueue(new Callback<FoodItem>() {
                    @Override
                    public void onResponse(Call<FoodItem> call, Response<FoodItem> response) {
                        dismissProgressDialog();
                        try {
                            FoodItem foodItem = response.body();
                            if (foodItem.getStatus() == 1) {

                                foodItemData.remove(position);
                                notifyDataSetChanged();
                                createToast(context, foodItem.getMsg()
                                        , Toast.LENGTH_SHORT);

                            } else {
                                createToast(context, foodItem.getMsg()
                                        , Toast.LENGTH_SHORT);
                            }

                        } catch (Exception e) {

                        }
                    }
                    @Override
                    public void onFailure(Call<FoodItem> call, Throwable t) {
                        dismissProgressDialog();

                    }
                });
            }
        }
    }

    private void setData(FoodItemAdapter.ViewHolder holder, int position) {
        holder.foodItemTitle.setText(foodItemData.get(position).getName());
        holder.foodItemDetails.setText(foodItemData.get(position).getDescription());
        holder.foodItemPrice.setText(foodItemData.get(position).getPrice());


        String imageUrl = foodItemData.get(position).getPhotoUrl();
        //Log.v("photo", imageUrl);
        if (customView == R.layout.custom_food_item){
            onLoadImageFromUrl(holder.foodItemImage, imageUrl, context);
        }else {
            onLoadImageFromUrl(holder.foodImage, imageUrl, context);
            if(foodItemData.get(position).getHasOffer()){
                holder.offerLine.setVisibility(View.VISIBLE);
                holder.foodItemOfferPrice.setText(foodItemData.get(position).getOfferPrice());
            }else {
                holder.offerLine.setVisibility(View.GONE);
                holder.foodItemOfferPrice.setVisibility(View.GONE);
            }
        }
        if (customView == R.layout.custom_food_item_room_db){
            holder.foodItemQuantity.setText(foodItemData.get(position).getQuantity()+"");
        }
    }

    @Override
    public int getItemCount() {
        return foodItemData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View view;

        public TextView foodItemTitle;
        public TextView foodItemDetails;
        public TextView foodItemPrice;
        public TextView foodItemOfferPrice;
        public TextView foodItemQuantity ;
        public View offerLine ;

        public CircleImageView foodItemImage;
        public ImageView foodImage;

        public ImageView delete;
        public ImageView update;
        public ImageView deleteFoodItem;

        public Button increaseFoodItem ;
        public Button decreaseFoodItem ;


        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            //ButterKnife.bind(this, view);
            foodItemTitle = itemView.findViewById(R.id.food_item_title);
            foodItemDetails= itemView.findViewById(R.id.food_item_details);
            foodItemPrice = itemView.findViewById(R.id.food_item_price);

            if(customView == R.layout.custom_food_item){
                delete = itemView.findViewById(R.id.food_item_delete);
                update = itemView.findViewById(R.id.food_item_update);
                foodItemImage = itemView.findViewById(R.id.food_item_image);

            }else  {
                foodImage = itemView.findViewById(R.id.food_item_image);
                foodItemOfferPrice = itemView.findViewById(R.id.food_item_offer_price);
                offerLine = itemView.findViewById(R.id.offer_line);
                if(customView == R.layout.custom_food_item_room_db){
                    deleteFoodItem = itemView.findViewById(R.id.food_item_delete);
                    foodItemQuantity = itemView.findViewById(R.id.offer_food_fragment_tv_display_quantity);
                    increaseFoodItem = itemView.findViewById(R.id.offer_food_fragment_btn_increase);
                    decreaseFoodItem = itemView.findViewById(R.id.offer_food_fragment_btn_decrease);
                }
            }
        }
    }
}