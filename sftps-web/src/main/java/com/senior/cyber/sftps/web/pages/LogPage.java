
package com.senior.cyber.sftps.web.pages;

import com.senior.cyber.sftps.dao.entity.Role;
import com.senior.cyber.sftps.web.MasterPage;
import com.senior.cyber.sftps.web.data.MySqlDataProvider;
import com.senior.cyber.frmk.common.base.Bookmark;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.AbstractDataTable;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.DataTable;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.cell.TextCell;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.Column;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.Expression;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.ItemPanel;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.convertor.DateTimeConvertor;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.convertor.LongConvertor;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.convertor.StringConvertor;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.translator.IHtmlTranslator;
import org.apache.commons.io.FileUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Bookmark("/log")
@AuthorizeInstantiation({Role.NAME_ROOT, Role.NAME_Page_Log})
public class LogPage extends MasterPage implements IHtmlTranslator<Tuple> {

    protected FilterForm<Map<String, Expression<?>>> log_browse_form;
    protected MySqlDataProvider log_browse_provider;
    protected List<IColumn<Tuple, String>> log_browse_column;
    protected AbstractDataTable<Tuple, String> log_browse_table;

    @Override
    protected void onInitData() {
        super.onInitData();
        this.log_browse_provider = new MySqlDataProvider("tbl_log");
        this.log_browse_provider.setSort("log_id", SortOrder.DESCENDING);
        this.log_browse_provider.setCountField("log_id");

        this.log_browse_column = new ArrayList<>();
        this.log_browse_column.add(Column.normalColumn(Model.of("ID"), "id", "log_id", this.log_browse_provider, new LongConvertor()));
        this.log_browse_column.add(Column.normalColumn(Model.of("Event"), "event_type", "event_type", this.log_browse_provider, new StringConvertor()));
        this.log_browse_column.add(Column.normalColumn(Model.of("User"), "user_display_name", "user_display_name", this.log_browse_provider, new StringConvertor()));
        this.log_browse_column.add(Column.normalColumn(Model.of("Key"), "key_name", "key_name", this.log_browse_provider, new StringConvertor()));
        this.log_browse_column.add(Column.normalColumn(Model.of("When"), "created_at", "created_at", this.log_browse_provider, new DateTimeConvertor()));
        this.log_browse_column.add(Column.normalColumn(Model.of("Size"), "size", "size", this.log_browse_provider, new LongConvertor(), this));
        this.log_browse_column.add(Column.normalColumn(Model.of("Src File"), "src_path", "src_path", this.log_browse_provider, new StringConvertor()));
        this.log_browse_column.add(Column.normalColumn(Model.of("Dst File"), "dst_path", "dst_path", this.log_browse_provider, new StringConvertor()));
    }

    @Override
    public ItemPanel htmlColumn(String key, IModel<String> display, Tuple object) {
        Long size = object.get("size", Long.class);
        if (size != null) {
            return new TextCell(FileUtils.byteCountToDisplaySize(size));
        } else {
            return new TextCell("");
        }
    }

    @Override
    protected void onInitHtml(MarkupContainer body) {
        this.log_browse_form = new FilterForm<>("log_browse_form", this.log_browse_provider);
        body.add(this.log_browse_form);

        this.log_browse_table = new DataTable<>("log_browse_table", this.log_browse_column,
                this.log_browse_provider, 20);
        this.log_browse_form.add(this.log_browse_table);
    }

}
