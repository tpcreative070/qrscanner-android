package com.jaychang.srv;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Utils {

  private static int gcd(int a, int b) {
    while (b > 0) {
      int temp = b;
      b = a % b;
      a = temp;
    }
    return a;
  }

  private static int lcm(int a, int b) {
    return a * (b / gcd(a, b));
  }

  static int lcm(List<Integer> input) {
    int result = input.get(0);
    for (int i = 1; i < input.size(); i++) {
      result = lcm(result, input.get(i));
    }
    return result;
  }

  static List<Integer> toIntList(String sequence) {
    char[] chars = sequence.toCharArray();
    List<Integer> result = new ArrayList<>(chars.length);
    for (char aChar : chars) {
      result.add(Integer.parseInt(aChar + ""));
    }
    return result;
  }

  static List<Integer> toIntList(int first, int... rest) {
    List<Integer> result = new ArrayList<>();
    result.add(first);
    for (int j : rest) {
      result.add(j);
    }
    return result;
  }

  public static int dpToPx(Context context, int dp) {
    float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
    return (int) (dp * density);
  }

  public static boolean isScrollable(RecyclerView recyclerView) {
    return recyclerView.computeHorizontalScrollRange() > recyclerView.getWidth() ||
      recyclerView.computeVerticalScrollRange() > recyclerView.getHeight();
  }

  public static Class<?> getTypeArgumentClass(Class<?> clazz) {
    Type type = ((ParameterizedType) Objects.requireNonNull(clazz.getGenericSuperclass())).getActualTypeArguments()[0];
    return (Class<?>) type;
  }

}
