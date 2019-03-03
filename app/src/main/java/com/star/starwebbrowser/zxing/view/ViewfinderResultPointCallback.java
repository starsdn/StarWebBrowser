package com.star.starwebbrowser.zxing.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

public final class ViewfinderResultPointCallback
  implements ResultPointCallback
{
  private final ViewfinderView viewfinderView;

  public ViewfinderResultPointCallback(ViewfinderView paramViewfinderView)
  {
    viewfinderView = paramViewfinderView;
  }

  public void foundPossibleResultPoint(ResultPoint paramResultPoint)
  {
    viewfinderView.addPossibleResultPoint(paramResultPoint);
  }
}

