package com.heinrichreimersoftware.materialintro.databinding;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
public abstract class MiFragmentSimpleSlideBinding extends ViewDataBinding {
    public final android.widget.TextView miDescription;
    public final android.widget.ImageView miImage;
    public final android.widget.TextView miTitle;
    // variables
    protected MiFragmentSimpleSlideBinding(android.databinding.DataBindingComponent bindingComponent, android.view.View root_, int localFieldCount
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
    public static MiFragmentSimpleSlideBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot) {
        return inflate(inflater, root, attachToRoot, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static MiFragmentSimpleSlideBinding inflate(android.view.LayoutInflater inflater) {
        return inflate(inflater, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static MiFragmentSimpleSlideBinding bind(android.view.View view) {
        return bind(view, android.databinding.DataBindingUtil.getDefaultComponent());
    }
    public static MiFragmentSimpleSlideBinding inflate(android.view.LayoutInflater inflater, android.view.ViewGroup root, boolean attachToRoot, android.databinding.DataBindingComponent bindingComponent) {
        return DataBindingUtil.<MiFragmentSimpleSlideBinding>inflate(inflater, com.heinrichreimersoftware.materialintro.R.layout.mi_fragment_simple_slide, root, attachToRoot, bindingComponent);
    }
    public static MiFragmentSimpleSlideBinding inflate(android.view.LayoutInflater inflater, android.databinding.DataBindingComponent bindingComponent) {
        return DataBindingUtil.<MiFragmentSimpleSlideBinding>inflate(inflater, com.heinrichreimersoftware.materialintro.R.layout.mi_fragment_simple_slide, null, false, bindingComponent);
    }
    public static MiFragmentSimpleSlideBinding bind(android.view.View view, android.databinding.DataBindingComponent bindingComponent) {
        return (MiFragmentSimpleSlideBinding)bind(bindingComponent, view, com.heinrichreimersoftware.materialintro.R.layout.mi_fragment_simple_slide);
    }
}