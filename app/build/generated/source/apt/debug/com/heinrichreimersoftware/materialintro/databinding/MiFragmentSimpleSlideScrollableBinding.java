package com.heinrichreimersoftware.materialintro.databinding;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
public abstract class MiFragmentSimpleSlideScrollableBinding extends ViewDataBinding {
    public final android.widget.TextView miDescription;
    public final android.widget.ImageView miImage;
    public final android.widget.TextView miTitle;
    // variables
    protected MiFragmentSimpleSlideScrollableBinding(android.databinding.DataBindingComponent bindingComponent, android.view.View root_, int localFieldCount
        , android.widget.TextView miDescription
        , android.widget.ImageView miImage
        , android.widget.TextView miTitle
    ) {
        super(bindingComponent, root_, localFieldCount);
        this.miDescription = miDescription;
        this.miImage = miImage;
        this.miTitle = miTitle;
    }
    //getters and abstract setters
    public static MiFragmentSimpleSlideScrollableBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot) {
        return inflate(inflater, root, attachToRoot, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static MiFragmentSimpleSlideScrollableBinding inflate(android.view.LayoutInflater inflater) {
        return inflate(inflater, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static MiFragmentSimpleSlideScrollableBinding bind(android.view.View view) {
        return bind(view, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static MiFragmentSimpleSlideScrollableBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot, android.databinding.DataBindingComponent bindingComponent) {
        return DataBindingUtil.<MiFragmentSimpleSlideScrollableBinding>inflate(inflater, com.heinrichreimersoftware.materialintro.R.layout.mi_fragment_simple_slide_scrollable, root, attachToRoot, bindingComponent);
    }
    public static MiFragmentSimpleSlideScrollableBinding inflate(android.view.LayoutInflater inflater, android.databinding.DataBindingComponent bindingComponent) {
        return DataBindingUtil.<MiFragmentSimpleSlideScrollableBinding>inflate(inflater, com.heinrichreimersoftware.materialintro.R.layout.mi_fragment_simple_slide_scrollable, null, false, bindingComponent);
    }
    public static MiFragmentSimpleSlideScrollableBinding bind(android.view.View view, android.databinding.DataBindingComponent bindingComponent) {
        return (MiFragmentSimpleSlideScrollableBinding)bind(bindingComponent, view, com.heinrichreimersoftware.materialintro.R.layout.mi_fragment_simple_slide_scrollable);
    }
}