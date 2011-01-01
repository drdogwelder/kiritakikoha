
package nz.net.catalyst.KiritakiKoha.search;

import java.util.ArrayList;

import nz.net.catalyst.KiritakiKoha.R;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    private Context context;

    private ArrayList<String> groups;

    private ArrayList<ArrayList<Record>> children;

    public ExpandableListAdapter(Context context, ArrayList<String> groups,
            ArrayList<ArrayList<Record>> children) {
        this.context = context;
        this.groups = groups;
        this.children = children;
    }

    public void addItem(Record rec) {
        if (!groups.contains(rec.getGroup())) {
            groups.add(rec.getGroup());
        }
        int index = groups.indexOf(rec.getGroup());
        if (children.size() < index + 1) {
            children.add(new ArrayList<Record>());
        }
        children.get(index).add(rec);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return children.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    // Return a child view. You can load your custom layout here.
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
    	Record rec = (Record) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.search_results_row_child, null);
        }
        TextView tv;
        tv = (TextView) convertView.findViewById(R.id.title);
        tv.setText(rec.getTitle());

        tv = (TextView) convertView.findViewById(R.id.description);
        tv.setText(Html.fromHtml(rec.getDescription()));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return children.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // Return a group view. You can load your custom layout here.
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        String group = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.search_results_row, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.title);
        tv.setText(group);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

}
