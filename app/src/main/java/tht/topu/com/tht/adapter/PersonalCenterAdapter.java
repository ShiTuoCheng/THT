package tht.topu.com.tht.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tht.topu.com.tht.R;

/**
 * Created by asus on 2017/8/8.
 */
public class PersonalCenterAdapter extends BaseAdapter {
    //当前对象
    private Context mContext;
    //k=1 金色 k=2 银色 k=3 黑色
    private int k=0;
    //图片需要传
    private int[] imgs;
    //文字
    //public String[] img_text = {mContext.getString(R.string.personal_all_order),mContext.getString(R.string.personal_pending_payment),mContext.getString(R.string.personal_shipment_pending),mContext.getString(R.string.personal_Shipped)};
    private String[] img_text = {"121","454","4554","4545"};
    //构造函数
    public PersonalCenterAdapter(Context mContext,int[] imgs,int k){
        super();
        this.mContext = mContext;
        this.k=k;
        this.imgs=imgs;

        this.img_text[0]=mContext.getString(R.string.personal_all_order);
        this.img_text[1]=mContext.getString(R.string.personal_pending_payment);
        this.img_text[2]=mContext.getString(R.string.personal_shipment_pending);
        this.img_text[3]=mContext.getString(R.string.personal_shipped);
    }
    @Override
    public int getCount() {

        return img_text.length;
    }
    @Override
    public Object getItem(int position) {

        return position;
    }
    @Override
    public long getItemId(int position) {

        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_personal_adapter, parent, false);
        }        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_personal_adapter, parent, false);
        }
        TextView tv = get(convertView,R.id.adapter_grid_text);
        ImageView iv = get(convertView,R.id.adapter_grid_pic);
        iv.setBackgroundResource(imgs[position]);

        //字体设置
        if(k==3) {
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.personal_adapter_grid_text_black));
        }
        else if(k==2){
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.personal_adapter_grid_text_silvery));
        }
        tv.setText(img_text[position]);
        return convertView;
    }

    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
