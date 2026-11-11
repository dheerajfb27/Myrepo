package com.braininvest.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private LinearLayout content;
    private int bg = Color.rgb(10, 15, 28), panel = Color.rgb(23, 32, 51), mint = Color.rgb(68, 220, 174);
    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg); show("Overview"); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(null, Typeface.BOLD); t.setPadding(0, 8, 0, 8); return t;
    }
    private void card(String heading, String detail) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(1); box.setPadding(18, 12, 18, 12); box.setBackgroundColor(panel);
        TextView h=text(heading,17,Color.WHITE,true), d=text(detail,14,Color.LTGRAY,false); box.addView(h); box.addView(d);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,8,0,8); content.addView(box,p);
    }
    private void show(String tab) {
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(18,14,18,10); root.setBackgroundColor(bg);
        root.addView(text("BRAIN-INVEST",13,mint,true)); root.addView(text("Think clearly. Invest wisely.",24,Color.WHITE,true));
        LinearLayout nav=new LinearLayout(this); nav.setOrientation(0);
        String[] tabs={"Overview","Stocks","Funds","Portfolio","Options"};
        for(String name:tabs){ Button b=new Button(this); b.setText(name); b.setTextSize(10); b.setAllCaps(false); b.setOnClickListener(v->show(name)); nav.addView(b,new LinearLayout.LayoutParams(0,-2,1)); }
        root.addView(nav);
        ScrollView scroll=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(1); content.setPadding(0,12,0,8); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(tab.equals("Overview")) {
            content.addView(text("Your market workspace",20,Color.WHITE,true));
            card("Market snapshot","NIFTY 50   •   SENSEX\nConnect a market-data provider to show live prices.");
            card("Watchlist","Add stocks to track price movement, key ratios and company news.");
            card("AI research","Get explainable research summaries, risks and scenario analysis — not guaranteed predictions.");
            card("Quick access","Stocks  •  Mutual funds  •  Portfolio  •  Options learning");
        } else if(tab.equals("Stocks")) {
            card("Stock screener","Filter by sector, market cap, valuation, growth and profitability. Live screening needs a data API.");
            card("Research checklist","Business quality • Debt • Cash flow • Valuation • Risks • Recent results");
            card("Watchlist","Your saved stocks will appear here when you add them.");
        } else if(tab.equals("Funds")) {
            card("Mutual fund explorer","Compare direct plans, expense ratios, tracking error, returns and risk.");
            card("Fund research","Review benchmark, portfolio overlap, consistency and downside risk before investing.");
        } else if(tab.equals("Portfolio")) {
            card("Portfolio tracker","Track holdings, average buy price, invested value, current value and allocation.");
            card("Privacy","This starter build stores no broker credentials and does not place trades.");
        } else {
            card("Options workspace","Learn calls, puts, payoff, breakeven, IV, Greeks and position risk.");
            card("Paper trading","Practice strategies with virtual positions before risking real money.");
            card("Risk first","Options can cause rapid losses. No live order placement is enabled in this build.");
        }
        content.addView(text("Starter version • Sample interface only • Not investment advice",11,Color.GRAY,false));
        setContentView(root);
    }
}
