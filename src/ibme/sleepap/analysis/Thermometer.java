package ibme.sleepap.analysis;

import ibme.sleepap.R;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class Thermometer {
	private Canvas canvas;
	private double originX;
	private double originY;
	private double zeroDegY;
	private double hundredDegY;
	private double mercuryWidth;
	private double arrowOffset;
	private double arrowWidth;
	private double textOffset;
	private double textSize;
	private Context context;

	public Thermometer(Context context, Canvas canvas) {
		this.context = context;
		this.canvas = canvas;
		double width = canvas.getWidth();
		double height = canvas.getHeight();

		this.originX = 0.1964 * width;
		this.originY = 0.9128 * height;
		this.zeroDegY = 0.7949 * height;
		this.hundredDegY = 0.0821 * height;
		this.mercuryWidth = 0.07 * width;
		this.arrowOffset = 0.357 * width;
		this.arrowWidth = 0.06 * height;
		this.textOffset = 0.55 * width;
		this.textSize = 0.085 * height;
	}

	public void drawThermometer(double fraction) {

		Paint paint = new Paint();

		// Mercury.
		paint.setColor(context.getResources().getColor(R.color.darkred));
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
		long rectBottom = Math.round(originY);
		long rectTop = Math.round(zeroDegY - ((zeroDegY - hundredDegY) * fraction));
		long rectLeft = Math.round(originX - (mercuryWidth / 2));
		long rectRight = Math.round(originX + (mercuryWidth / 2));
		canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

		// Marker.
		if (fraction < 0.5) {
			paint.setColor(context.getResources().getColor(R.color.darkgreen));
		}
		long arrowPointX = Math.round(arrowOffset);
		long arrowPointY = rectTop;
		long arrowRight = Math.round(arrowOffset + arrowWidth);
		long arrowTopY = Math.round(rectTop - (arrowWidth / 2));
		long arrowBottomY = Math.round(rectTop + (arrowWidth / 2));
		Path marker = new Path();
		marker.moveTo(arrowPointX, arrowPointY);
		marker.lineTo(arrowRight, arrowTopY);
		marker.lineTo(arrowRight, arrowBottomY);
		marker.lineTo(arrowPointX, arrowPointY);
		canvas.drawPath(marker, paint);

		// Text.
		long textX = Math.round(textOffset);
		long textY = arrowBottomY;
		paint.setTextSize(Math.round(textSize));
		String percentage = String.format(Locale.getDefault(), "%.0f%%", fraction * 100);
		canvas.drawText(percentage, textX, textY, paint);

	}
}
