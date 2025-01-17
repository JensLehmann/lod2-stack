/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.lod2.stat.dsdrepo;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import eu.lod2.stat.datacube.Attribute;
import eu.lod2.stat.datacube.CodeList;
import eu.lod2.stat.datacube.ComponentProperty;
import eu.lod2.stat.datacube.DataCubeGraph;
import eu.lod2.stat.datacube.DataSet;
import eu.lod2.stat.datacube.Dimension;
import eu.lod2.stat.datacube.Measure;
import eu.lod2.stat.datacube.Structure;
import eu.lod2.stat.datacube.sparql_impl.SparqlDCGraph;
import eu.lod2.stat.datacube.sparql_impl.SparqlDCRepository;
import eu.lod2.stat.datacube.sparql_impl.SparqlDataSet;
import eu.lod2.stat.datacube.sparql_impl.SparqlStructure;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author vukm
 */
public class DSDRepoComponent extends CustomComponent {
    private Repository repository;
    private DataSet ds;
    private VerticalLayout mainLayout;
    private HorizontalLayout datasetLayout;
    private String endpoint;
    private SparqlDCRepository dcRepo;
    private HorizontalLayout contentLayout;
    private ComboBox selectDataSet;
    private DataCubeGraph graph;
    private DSDRepo dsdRepo;
    private Tree dataTree;
    private Tree repoTree;
    
    private String dataGraph;
    private String dataset;
    private String repoGraph;
    private String highlighted;
    
    private ThemeResource icon_property = new ThemeResource("icons/icon-prop-blue_16.png");
    private ThemeResource icon_structure = new ThemeResource("icons/icon-struct-blue_16.png");
    
    private static final Action ACTION_SET_AS_DIM = new Action("Set as Dimension");
    private static final Action ACTION_SET_AS_MEAS = new Action("Set as Measure");
    private static final Action ACTION_SET_AS_ATTR = new Action("Set as Attribute");
    private static final Action ACTION_SET_AS_UNDEF = new Action("Set as Undefined");
    private static final Action ACTION_HIGHLIGHT_MATCHING = new Action("Highlight Matching");
    private static final Action ACTION_SET_AS_DSD = new Action("Set as qb:DataStructureDefinition");
    
    private static final Action ACTION_EXPAND_ALL = new Action("Expand All");
    private static final Action ACTION_COLLAPSE_ALL = new Action("Collapse All");
    private static final Action [] ACTIONS_NAVI = new Action [] { ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL };
    
    private static final Action ACTION_CREATE_CL = new Action("Create Code List");
    private static final Action ACTION_DELETE_CL = new Action("Delete Code List");
    private static final Action ACTION_SET_AS_CL = new Action("Set as Code List");
    
    private static final Action ACTION_STORE = new Action("Store DSD in repository");

    private static final Action [] ACTIONS = new Action[] { 
        ACTION_SET_AS_DIM, ACTION_SET_AS_MEAS, ACTION_SET_AS_ATTR, 
        ACTION_SET_AS_UNDEF, ACTION_HIGHLIGHT_MATCHING };
    
    private static final Action [] ACTIONS_NAVI_PLUS = new Action [] {
        ACTION_SET_AS_DIM, ACTION_SET_AS_MEAS, ACTION_SET_AS_ATTR, 
        ACTION_SET_AS_UNDEF, ACTION_HIGHLIGHT_MATCHING,
        ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL
    };
    private static final Action [] ACTIONS_DSD_NAVI = new Action [] {
        ACTION_SET_AS_DSD, ACTION_EXPAND_ALL, ACTION_COLLAPSE_ALL
    };
    
    private VerticalLayout statusLayout;
    
    private MenuBar.Command cmdFindDSD;
    private MenuBar.Command cmdCreateDSD;
    private CountingTreeHeader dim;
    private CountingTreeHeader meas;
    private CountingTreeHeader attr;
    private CountingTreeHeader undef;
    private Tree compatibleCodeLists;
    private MenuBar.Command cmdStoreDSD;
    
    private int numUndefinedComponents = 0;
    private int numMissingCodeLists = 0;
    private Label lblMissingCodeLists;
    private Label lblUndefined;
    private LinkedList<Structure> repoTreeItems;
    
    private void updateUndefinedAndMissing(){
        int num = 0;
        for (Object obj: dataTree.rootItemIds()){
            CountingTreeHeader h = (CountingTreeHeader) obj;
            if (h.getHeader().toString().startsWith("U"))
                numUndefinedComponents = h.getCount();
            
            num += countMissingCodeListsInHeader(dataTree, h);
        }
        numMissingCodeLists = num;
        lblUndefined.setValue("There are still " + numUndefinedComponents + " undefined components");
        lblMissingCodeLists.setValue("There are still " + numMissingCodeLists + " missing code lists");
    }
    
    private int countMissingCodeListsInHeader(Tree tree, CountingTreeHeader h){
        int c = 0;
        Collection<?> children = tree.getChildren(h);
        if (children != null) {
            for (Object obj: children){
                // obj is either dimension, measure or attribute
                Collection<?> infants = tree.getChildren(obj);
                if (infants.size() != 2 && infants.iterator().next().toString().startsWith("C"))
                    c++;
            }
        }
        return c;
    }
    
    private class CodeItem {
        private String code;
        public CodeItem (String code){
            this.code = code;
        }
        @Override
        public String toString() {
            return code;
        }
        
    }
    
    private class CodeListUriWindow extends Window {
        private String uri = null;
        public String getUri() {
            return uri;
        }
        public void show() {
            DSDRepoComponent.this.getWindow().addWindow(CodeListUriWindow.this);
        }
        public CodeListUriWindow (){
            setCaption("Enter URI");
            setModal(true);
            setClosable(false);
            setWidth("450px");
            final TextField field = new TextField("Enter code list URI");
            addComponent(field);
            field.setWidth("100%");
            HorizontalLayout btnLayout = new HorizontalLayout();
            addComponent(btnLayout);
            Button ok = new Button("OK");
            btnLayout.addComponent(ok);
            Button cancel = new Button("Cancel");
            btnLayout.addComponent(cancel);
            ok.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    uri = field.getValue().toString();
                    if (!isUri(uri)) {
                        DSDRepoComponent.this.getWindow().showNotification("Not a valid URI", Notification.TYPE_ERROR_MESSAGE);
                        uri = null;
                        return;
                    }
                    DSDRepoComponent.this.getWindow().removeWindow(CodeListUriWindow.this);
                }
            });
            cancel.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    uri = null;
                    DSDRepoComponent.this.getWindow().removeWindow(CodeListUriWindow.this);
                }
            });
        }
    }
    
    public DSDRepoComponent(Repository repository, String dataGraph){
        this(repository, dataGraph, "http://lod2statworkbench/dsd-repository/");
    }
    
    private void initializeRepoGraph(){
        try {
            RepositoryConnection conn = repository.getConnection();
            String query = "CREATE SILENT GRAPH <" + repoGraph + ">";
            conn.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate();
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DSDRepoComponent(Repository repository, String dataGraph, String repoGraph){
        this.repository = repository;
        this.dataGraph = dataGraph;
        this.repoGraph = repoGraph;
        
        initializeRepoGraph();
        
        dcRepo = new SparqlDCRepository(repository);
        graph = new SparqlDCGraph(repository, dataGraph);
        
        setSizeFull();
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        setDebugId("dsd-repo");
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeUndefined();
        mainLayout.setWidth("100%");
//        mainLayout.setHeight("800px");
        mainLayout.setSpacing(true);
        
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.setSpacing(true);
        menuLayout.setWidth("100%");
        rootLayout.addComponent(menuLayout);
        rootLayout.setExpandRatio(menuLayout, 0.0f);
        
        final MenuBar menu = new MenuBar();
        menu.addStyleName("dsd");
        cmdFindDSD = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                for (MenuBar.MenuItem item: menu.getItems()){
                    if (item == selectedItem){
                        if (!item.getStyleName().contains("selected")) {
                            if (ds != null ) item.setStyleName("selected");
                            findDSDs();
                        }
                    } else item.setStyleName("bleja");
                }
            }
        };
        menu.addItem("Find Suitable DSDs", cmdFindDSD).setStyleName("bleja");
        cmdCreateDSD = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                for (MenuBar.MenuItem item: menu.getItems()){
                    if (item == selectedItem){
                        if (!item.getStyleName().contains("selected")) {
                            if (ds != null ) item.setStyleName("selected");
                            createDSD();
                        }
                    } else item.setStyleName("bleja");
                }
            }
        };
        menu.addItem("Create DSD", cmdCreateDSD).setStyleName("bleja");
        cmdStoreDSD = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                for (MenuBar.MenuItem item: menu.getItems()){
                    if (item == selectedItem){
                        if (!item.getStyleName().contains("selected")) {
                            if (ds != null ) item.setStyleName("selected");
                            storeDSD();
                        }
                    } else item.setStyleName("bleja");
                }
            }
        };
        menu.addItem("Store DSD", cmdStoreDSD).setStyleName("bleja");
        
        menuLayout.addComponent(menu);
        Label spaceLbl = new Label("");
        menuLayout.addComponent(spaceLbl);
        menuLayout.setExpandRatio(spaceLbl, 2.0f);
        Label lbl = new Label("Choose dataset: ");
        lbl.setSizeUndefined();
        menuLayout.addComponent(lbl);
        
        Collection<DataSet> colDataSets = graph.getDataSets();
        if (colDataSets == null) colDataSets = new LinkedList<DataSet>();
        selectDataSet = new ComboBox(null, colDataSets);
        selectDataSet.setImmediate(true);
        selectDataSet.setNewItemsAllowed(false);
        selectDataSet.setNullSelectionAllowed(false);
        selectDataSet.setWidth("300px");
        selectDataSet.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                ds = (DataSet) event.getProperty().getValue();
            }
        });
        menuLayout.addComponent(selectDataSet);
        
        Panel mainPanel = new Panel(mainLayout);
        mainPanel.setSizeFull();
        mainPanel.setScrollable(true);
        mainPanel.setStyleName(Reindeer.PANEL_LIGHT);
        
        Label hrLabel = new Label("<hr/>", Label.CONTENT_XHTML);
        rootLayout.addComponent(hrLabel);
        rootLayout.setExpandRatio(hrLabel, 0.0f);
        rootLayout.addComponent(mainPanel);
        rootLayout.setExpandRatio(mainPanel, 2.0f);
        rootLayout.setMargin(true, false, true, false);
        
        setCompositionRoot(rootLayout);
    }
    
    private void findDSDs(){
        mainLayout.removeAllComponents();
        
        contentLayout = new HorizontalLayout();
//        contentLayout.setSizeFull();
        contentLayout.setSizeUndefined();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        refreshContentFindDSDs(ds);
    }
    
    private void createDSD(){
        mainLayout.removeAllComponents();
        
        contentLayout = new HorizontalLayout();
//        contentLayout.setSizeFull();
        contentLayout.setSizeUndefined();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        refreshContentCreateDSD(ds);
    }
    
    private void storeDSD(){
        mainLayout.removeAllComponents();
        
        contentLayout = new HorizontalLayout();
//        contentLayout.setSizeFull();
        contentLayout.setSizeUndefined();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        mainLayout.addComponent(contentLayout);
        refreshContentStoreDSD(ds);
    }
    
    private void createStatusLayout(){
        
    }
    
    private CountingTreeHeader createCountingTreeHeader(Tree t, String header){
        CountingTreeHeader h = new CountingTreeHeader(t, header);
        t.addItem(h);
        return h;
    }
    
    private String generatePropertiesTable(String uri, String graph){
        StringBuilder builder = new StringBuilder();
        try {
            RepositoryConnection conn = repository.getConnection();
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qResourcePorperties(uri, graph));
            TupleQueryResult res = query.evaluate();
            builder.append("<table style=\"border-spacing:7px\">");
            while (res.hasNext()) {
                BindingSet set = res.next();
                builder.append("<tr><td>");
                builder.append(set.getValue("p").stringValue());
                builder.append("</td><td>");
                builder.append(set.getValue("o").stringValue());
                builder.append("</td></tr>");
            }
            builder.append("</table>");
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
        return builder.toString();
    }
    
    private void populateDataTree(){
        dataTree.removeAllItems();
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qPossibleComponents(dataGraph, dataset));
            TupleQueryResult res = q.evaluate();
            
            dim = createCountingTreeHeader(dataTree, "Dimensions");
            meas = createCountingTreeHeader(dataTree, "Measures");
            attr = createCountingTreeHeader(dataTree, "Attributes");
            undef = createCountingTreeHeader(dataTree, "Undefined");
            boolean indHasData = res.hasNext();
            
            while (res.hasNext()){
                BindingSet set = res.next();
                String component = set.getValue("comp").stringValue();
//                undef.addElement(component);
                addItemProperty(dataTree, component);
                dataTree.setParent(component, undef);
            }
            
            dataTree.addListener(new Tree.ExpandListener() {
                public void nodeExpand(Tree.ExpandEvent event) {
                    Object obj = event.getItemId();
                    if (!(obj instanceof String)) return;
                    if (dataTree.hasChildren(obj)) return;
                    String component = (String)obj;
                    
                    try {
                        RepositoryConnection con = repository.getConnection();
                        TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qCodesTypes(component, dataset, dataGraph));
                        TupleQueryResult res = q.evaluate();
                        Collection<String> values = new LinkedList<String>();
                        Collection<String> datatypes = new HashSet<String>();
                        int count = 0;
                        
                        while (res.hasNext()){
                            BindingSet set = res.next();
                            String val = set.getValue("val").stringValue();
                            values.add(val);
                            String iri = set.getValue("iri").stringValue();
                            Value datatypeObj = set.getValue("datatype");
                            if (datatypeObj != null) datatypes.add(datatypeObj.stringValue());
                            count += Integer.valueOf(iri).intValue();
                        }
                        
                        CodeDatatypeTreeElement elem = null;
                        if (count > 0 && count < values.size()) {
                            elem = new CodeDatatypeTreeElement("", false, 1);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        else if (count==0 && datatypes.size()!=1) {
                            elem = new CodeDatatypeTreeElement("", false, 1);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        else if (count==0){
                            CountingTreeHeader countTypes = createCountingTreeHeader(dataTree, "Datatypes");
                            dataTree.setParent(countTypes, obj);
                            String e = datatypes.iterator().next();
                            elem = new CodeDatatypeTreeElement(e, false, 0);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, countTypes);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                        else if (count == values.size()){
                            CountingTreeHeader countValues = createCountingTreeHeader(dataTree, "Codes");
                            dataTree.setParent(countValues, obj);
                            for (String element: values){
                                CodeDatatypeTreeElement e = new CodeDatatypeTreeElement(element, true, 0);
                                dataTree.addItem(e);
                                dataTree.setParent(e, countValues);
                                dataTree.setChildrenAllowed(e, false);
                            }
                        } else {
                            elem = new CodeDatatypeTreeElement("", false, 3);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, obj);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            });
            dataTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
                public String generateDescription(Component source, Object itemId, Object propertyId) {
                    // no description for Counting elements
                    if (itemId instanceof CountingTreeHeader) return null;
                    // URI is 
                    String uri = itemId.toString();
                    StringBuilder builder = new StringBuilder();
                    builder.append("<h2>Properties of ");
                    builder.append(itemId);
                    builder.append("</h2><br>");
                    builder.append(generatePropertiesTable(uri, dataGraph));
                    return builder.toString();
                }
            });
            if (indHasData){
                dataTree.expandItemsRecursively(undef);
                dataTree.collapseItemsRecursively(undef);
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
    }
    
    private void addDataTreeListenersStore(){
        dataTree.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target instanceof Structure)
                    return new Action [] { ACTION_STORE };
                else 
                    return null;
            }
            public void handleAction(Action action, Object sender, Object target) {
                if (action == null) return;
                if (action == ACTION_STORE) {
                    try {
                        String dsd = ds.getStructure().getUri();
                        RepositoryConnection conn = repository.getConnection();
                        for (String qString: DSDRepoUtils.qCopyDSD(dsd, dataGraph, repoGraph)){
                            GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, qString);
                            query.evaluate();
                            getWindow().showNotification("DSD stored");
                        }
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    }
                    
                }
            }
        });
    }
    
    private void addDataTreeListenersFind(){
        dataTree.addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (dataTree.hasChildren(target))
                    if (target instanceof String) {
                        return ACTIONS_NAVI_PLUS;
                    } else {
                        return ACTIONS_NAVI;
                    }
                else {
                    if (target instanceof String) {
                        return ACTIONS;
                    } else {
                        return null;
                    }
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (!(target instanceof String || target instanceof CountingTreeHeader)) return;
                String e = (target instanceof String)?(String)target:null;
                if (action == ACTION_SET_AS_DIM) {
                    dataTree.setParent(e, dim);
                    refreshRepoTree();
                }
                else if (action == ACTION_SET_AS_MEAS){
                    dataTree.setParent(e, meas);
                    refreshRepoTree();
                } else if (action == ACTION_SET_AS_ATTR){
                    dataTree.setParent(e, attr);
                    refreshRepoTree();
                } else if (action == ACTION_SET_AS_UNDEF){
                    dataTree.setParent(e, undef);
                    refreshRepoTree();
                } else if (action == ACTION_HIGHLIGHT_MATCHING){
                    // notify repo tree about the change
//                        highlighted = e;
                    // update repo tree
//                        repoTree.containerItemSetChange(null);
                } else if (action == ACTION_EXPAND_ALL)
                    dataTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    dataTree.collapseItemsRecursively(target);
            }
        });
        
        dataTree.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                Object selected = dataTree.getValue();
                if (selected instanceof String){
                    for (Object item: repoTree.getItemIds()){
                        if (item instanceof ComponentProperty){
                            ComponentProperty cp = (ComponentProperty) item;
                            if (cp.getUri().equalsIgnoreCase(selected.toString())){
                                repoTree.select(item);
                                Object parent = repoTree.getParent(item);
                                while (parent != null){
                                    repoTree.expandItem(parent);
                                    parent = repoTree.getParent(parent);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        });
    }
    
    private void addDataTreeListenersCreate(){
        dataTree.addActionHandler(new Action.Handler() {
            // TODO: add options to create and delete code lists

            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                LinkedList<Action> actions = new LinkedList<Action>();
                if (dataTree.hasChildren(target))
                    if (target instanceof String) {
                        for (Action a: ACTIONS_NAVI_PLUS) actions.add(a);
                        Collection<?> children = dataTree.getChildren(target);
                        if (children.size() == 2) 
                            actions.add(ACTION_DELETE_CL);
                        else if (children.iterator().next().toString().startsWith("C"))
                            actions.add(ACTION_CREATE_CL);
                    } else {
                        for (Action a: ACTIONS_NAVI) actions.add(a);
                    }
                else {
                    if (target instanceof String) {
                        for (Action a: ACTIONS) actions.add(a);
                    }
                }
                return actions.toArray(new Action [] {});
            }

            public void handleAction(Action action, Object sender, final Object target) {
                if (!(target instanceof String || target instanceof CountingTreeHeader)) return;
                String e = (target instanceof String)?(String)target:null;
                if (action == ACTION_SET_AS_DIM) {
                    dataTree.setParent(e, dim);
                    updateUndefinedAndMissing();
                }
                else if (action == ACTION_SET_AS_MEAS){
                    dataTree.setParent(e, meas);
                    updateUndefinedAndMissing();
                } else if (action == ACTION_SET_AS_ATTR){
                    dataTree.setParent(e, attr);
                    updateUndefinedAndMissing();
                } else if (action == ACTION_SET_AS_UNDEF){
                    dataTree.setParent(e, undef);
                    updateUndefinedAndMissing();
                } else if (action == ACTION_HIGHLIGHT_MATCHING){
                    // notify repo tree about the change
//                        highlighted = e;
                    // update repo tree
//                        repoTree.containerItemSetChange(null);
                } else if (action == ACTION_EXPAND_ALL)
                    dataTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    dataTree.collapseItemsRecursively(target);
                else if (action == ACTION_CREATE_CL){
                    final String prop = target.toString();
                    Collection<?> children = dataTree.getChildren(target);
                    final Collection<String> codes = new LinkedList<String>();
                    for (Object child: children){
                        if (child.toString().startsWith("Codes")){
                            for (Object obj: dataTree.getChildren(child))
                                codes.add(obj.toString());
                        }
                    }
                    // get code list URI from user
                    final CodeListUriWindow uriWindow = new CodeListUriWindow();
                    uriWindow.addListener(new Window.CloseListener() {
                        public void windowClose(Window.CloseEvent e) {
                            String uri = uriWindow.getUri();
                            if (uri == null) {
                                getWindow().showNotification("Not a valid URI", Window.Notification.TYPE_ERROR_MESSAGE);
                                return;
                            }
                            try {
                                RepositoryConnection conn = repository.getConnection();
                                // insert dataset link
                                String insertQuery = "INSERT INTO GRAPH <" + dataGraph + 
                                        "> { <" + prop + "> <http://purl.org/linked-data/cube#codeList> <" + uri + "> }";
                                conn.prepareGraphQuery(QueryLanguage.SPARQL, insertQuery).evaluate();
                                // insert the rest
                                GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, DSDRepoUtils.qCreateCodeList(dataGraph, prop, uri, codes));
                                query.evaluate();
                                Object codesItem = dataTree.getChildren(target).iterator().next();
                                Collection<?> codesForCodeList = dataTree.getChildren(codesItem);
                                CountingTreeHeader headerCodeList = new CountingTreeHeader(dataTree, "Code List");
                                dataTree.addItem(headerCodeList);
                                dataTree.setParent(headerCodeList, target);
                                for (Object elem: codesForCodeList){
                                    CodeItem ci = new CodeItem(elem.toString());
                                    dataTree.addItem(ci);
                                    dataTree.setParent(ci, headerCodeList);
                                }
                                updateUndefinedAndMissing();
                            } catch (RepositoryException ex) {
                                Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                                getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                            } catch (MalformedQueryException ex) {
                                Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                                getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                            } catch (QueryEvaluationException ex) {
                                Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                                getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                            }
                        }
                    });
                    uriWindow.show();
                    
                } else if (action == ACTION_DELETE_CL){
                    String prop = target.toString();
                    Collection<?> children = dataTree.getChildren(target);
                    final Collection<String> codes = new LinkedList<String>();
                    final Collection<Object> items = new LinkedList<Object>();
                    Object itemsHeader = null;
                    for (Object child: children){
                        if (child.toString().startsWith("Code List")){
                            itemsHeader = child;
                            for (Object obj: dataTree.getChildren(child)) {
                                codes.add(obj.toString());
                                items.add(obj);
                            }
                        }
                    }
                    try {
                        RepositoryConnection conn = repository.getConnection();
                        GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, DSDRepoUtils.qDeleteCodeList(dataset, prop, "uri", codes));
                        query.evaluate();
                        for (Object o: items) dataTree.removeItem(o);
                        dataTree.removeItem(itemsHeader);
                        updateUndefinedAndMissing();
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            }
        });
        
        // show compatible code lists when a user selects a dim with code lists
        dataTree.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                Object selected = dataTree.getValue();
                if (selected instanceof String){
                    Object child = dataTree.getChildren(selected).iterator().next();
                    Object infant = dataTree.getChildren(child).iterator().next();
                    CodeDatatypeTreeElement elem = (CodeDatatypeTreeElement)infant;
                    if (!elem.isCode()) {
                        compatibleCodeLists.removeAllItems();
                        return;
                    }
                    
                    try {
                        // get compatible code lists and show in compatibleCodeLists tree
                        RepositoryConnection conn = repository.getConnection();
                        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, 
                                DSDRepoUtils.qCompatibleCodeLists(selected.toString(), dataGraph, repoGraph));
                        TupleQueryResult res = query.evaluate();
                        Collection<String> codeLists = new LinkedList<String>();
                        while (res.hasNext()){
                            BindingSet set = res.next();
                            String cl = set.getValue("cl").stringValue();
                            codeLists.add(cl);
                        }
                        compatibleCodeLists.setData(selected);
                        populateCodeListTree(codeLists);
                        if (codeLists.size()>0) {
                            compatibleCodeLists.setValue(codeLists.iterator().next());
                        }
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            }
        });
    }
    
    private void addItemStructure(Tree t, Structure s){
        t.addItem(s);
        t.setItemIcon(s, icon_structure);
    }
    
    private void addItemProperty(Tree t, Object p){
        t.addItem(p);
        t.setItemIcon(p, icon_property);
    }
    
    private void populateStoreTree(){
        dataTree.removeAllItems();
        String dsd = ds.getStructure().getUri();
        Structure structure = ds.getStructure();
        addItemStructure(dataTree, structure);
        
        CountingTreeHeader dimCountHeader = createCountingTreeHeader(dataTree, "Dimensions");
        dataTree.setParent(dimCountHeader, structure);
        CountingTreeHeader measCountHeader = createCountingTreeHeader(dataTree, "Measures");
        dataTree.setParent(measCountHeader, structure);
        CountingTreeHeader attrCountHeader = createCountingTreeHeader(dataTree, "Attributes");
        dataTree.setParent(attrCountHeader, structure);
        
        for (Dimension dim: structure.getDimensions()){
            addItemProperty(dataTree, dim);
            dataTree.setParent(dim, dimCountHeader);
        }
        for (Attribute attr: structure.getAttributes()){
            addItemProperty(dataTree, attr);
            dataTree.setParent(attr, attrCountHeader);
        }
        for (Measure meas: structure.getMeasures()){
            addItemProperty(dataTree, meas);
            dataTree.setParent(meas, measCountHeader);
        }
        
        dataTree.addListener(new Tree.ExpandListener() {
            public void nodeExpand(Tree.ExpandEvent event) {
                Object id = event.getItemId();
                if (id instanceof ComponentProperty && !dataTree.hasChildren(id)) {
                    ComponentProperty prop = (ComponentProperty) id;
                    CodeList codeList = prop.getCodeList();
                    if (codeList != null) {
                        CountingTreeHeader codeCountingHeader = createCountingTreeHeader(dataTree, "Codes");
                        dataTree.setParent(codeCountingHeader, id);
                        for (String code: codeList.getAllCodes()){
                            CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(code, true, 0);
                            dataTree.addItem(elem);
                            dataTree.setParent(elem, codeCountingHeader);
                            dataTree.setChildrenAllowed(elem, false);
                        }
                    } else {
                        CountingTreeHeader datatypes = createCountingTreeHeader(dataTree, "Datatypes");
                        dataTree.setParent(datatypes, id);
                        CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(prop.getRange(), false, 0);
                        dataTree.addItem(elem);
                        dataTree.setParent(elem, datatypes);
                        dataTree.setChildrenAllowed(elem, false);
                    }
                }
            }
        });
        
        dataTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                // description only for Counting elements ComponentProperties and Codes/TYpes
                String uri = null;
                if (itemId instanceof ComponentProperty)
                    uri = ((ComponentProperty)itemId).getUri();
                else if (itemId instanceof CodeDatatypeTreeElement)
                    uri = ((CodeDatatypeTreeElement)itemId).getValue();
                else return null;
                
                // URI is 
                StringBuilder builder = new StringBuilder();
                builder.append("<h2>Properties of ");
                builder.append(uri);
                builder.append("</h2><br>");
                builder.append(generatePropertiesTable(uri, repoGraph));
                return builder.toString();
            }
        });
    }
    
    private boolean isListInDimensions(List<String> list, Collection<Dimension> props){
        int numProps = (props==null)?0:props.size();
        int numMems = (list==null)?0:list.size();
        if (numMems>numProps) return false;
        if (numMems == 0) return true;
        for (String member: list){
            boolean ind = false;
            for (Dimension p: props){
                if (p.getUri().equalsIgnoreCase(member)) ind = true;
            }
            if (!ind) return false;
        }
        return true;
    }
    
    private boolean isListInMeasures(List<String> list, Collection<Measure> props){
        int numProps = (props==null)?0:props.size();
        int numMems = (list==null)?0:list.size();
        if (numMems>numProps) return false;
        if (numMems == 0) return true;
        for (String member: list){
            boolean ind = false;
            for (Measure p: props){
                if (p.getUri().equalsIgnoreCase(member)) ind = true;
            }
            if (!ind) return false;
        }
        return true;
    }
    
    private boolean isListInAttributes(List<String> list, Collection<Attribute> props){
        int numProps = (props==null)?0:props.size();
        int numMems = (list==null)?0:list.size();
        if (numMems>numProps) return false;
        if (numMems == 0) return true;
        for (String member: list){
            boolean ind = false;
            for (Attribute p: props){
                if (p.getUri().equalsIgnoreCase(member)) ind = true;
            }
            if (!ind) return false;
        }
        return true;
    }
    
    private void refreshRepoTree(){
        repoTree.removeAllItems();
        LinkedList<String> dList = new LinkedList<String>();
        LinkedList<String> mList = new LinkedList<String>();
        LinkedList<String> aList = new LinkedList<String>();
        for (Object id: dataTree.rootItemIds()){
            Collection<?> children = dataTree.getChildren(id);
            if (children == null) continue;

            Collection<String> list = null;
            if (id.toString().startsWith("D")) list = dList;
            else if (id.toString().startsWith("M")) list = mList;
            else if (id.toString().startsWith("A")) list = aList;
            else continue;

            for (Object prop: dataTree.getChildren(id)){
                list.add(prop.toString());
            }
        }
        
        for (Structure structure: repoTreeItems){
            // if any of the dimensions, measures or attribs are not present skip
            if (!isListInDimensions(dList, structure.getDimensions())) continue;
            if (!isListInMeasures(mList, structure.getMeasures())) continue;
            if (!isListInAttributes(aList, structure.getAttributes())) continue;
            
            // add structure to the tree
            addItemStructure(repoTree, structure);
            CountingTreeHeader dimCountHeader = createCountingTreeHeader(repoTree, "Dimensions");
            repoTree.setParent(dimCountHeader, structure);
            CountingTreeHeader measCountHeader = createCountingTreeHeader(repoTree, "Measures");
            repoTree.setParent(measCountHeader, structure);
            CountingTreeHeader attrCountHeader = createCountingTreeHeader(repoTree, "Attributes");
            repoTree.setParent(attrCountHeader, structure);

            for (Dimension dim: structure.getDimensions()){
                addItemProperty(repoTree, dim);
                repoTree.setParent(dim, dimCountHeader);
            }
            for (Attribute attr: structure.getAttributes()){
                addItemProperty(repoTree, attr);
                repoTree.setParent(attr, attrCountHeader);
            }
            for (Measure meas: structure.getMeasures()){
                addItemProperty(repoTree, meas);
                repoTree.setParent(meas, measCountHeader);
            }
        }
    }
    
    private void populateRepoTree(){
        repoTree.removeAllItems();
        repoTreeItems.clear();
        try {
            RepositoryConnection con = repository.getConnection();
            TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qMatchingStructures(dataGraph, dataset, repoGraph));
            TupleQueryResult res = q.evaluate();
            
            while (res.hasNext()){
                BindingSet set = res.next();
                String dsd = set.getValue("dsd").stringValue();
                final Structure structure = new SparqlStructure(repository, dsd, repoGraph);
                addItemStructure(repoTree, structure);
                repoTreeItems.add(structure);
                CountingTreeHeader dimCountHeader = createCountingTreeHeader(repoTree, "Dimensions");
                repoTree.setParent(dimCountHeader, structure);
                CountingTreeHeader measCountHeader = createCountingTreeHeader(repoTree, "Measures");
                repoTree.setParent(measCountHeader, structure);
                CountingTreeHeader attrCountHeader = createCountingTreeHeader(repoTree, "Attributes");
                repoTree.setParent(attrCountHeader, structure);
                
                for (Dimension dim: structure.getDimensions()){
                    addItemProperty(repoTree, dim);
                    repoTree.setParent(dim, dimCountHeader);
                }
                for (Attribute attr: structure.getAttributes()){
                    addItemProperty(repoTree, attr);
                    repoTree.setParent(attr, attrCountHeader);
                }
                for (Measure meas: structure.getMeasures()){
                    addItemProperty(repoTree, meas);
                    repoTree.setParent(meas, measCountHeader);
                }
            }
        } catch (RepositoryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (MalformedQueryException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
            getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
        
        repoTree.addListener(new Tree.ExpandListener() {
            public void nodeExpand(Tree.ExpandEvent event) {
                Object id = event.getItemId();
                if (id instanceof ComponentProperty && !repoTree.hasChildren(id)) {
                    ComponentProperty prop = (ComponentProperty) id;
                    CodeList codeList = prop.getCodeList();
                    if (codeList != null) {
                        CountingTreeHeader codeCountingHeader = createCountingTreeHeader(repoTree, "Codes");
                        repoTree.setParent(codeCountingHeader, id);
                        for (String code: codeList.getAllCodes()){
                            CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(code, true, 0);
                            repoTree.addItem(elem);
                            repoTree.setParent(elem, codeCountingHeader);
                            repoTree.setChildrenAllowed(elem, false);
                        }
                    } else {
                        CountingTreeHeader datatypes = createCountingTreeHeader(repoTree, "Datatypes");
                        repoTree.setParent(datatypes, id);
                        CodeDatatypeTreeElement elem = new CodeDatatypeTreeElement(prop.getRange(), false, 0);
                        repoTree.addItem(elem);
                        repoTree.setParent(elem, datatypes);
                        repoTree.setChildrenAllowed(elem, false);
                    }
                }
            }
        });
        
        repoTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                // description only for Counting elements ComponentProperties and Codes/TYpes
                String uri = null;
                if (itemId instanceof ComponentProperty)
                    uri = ((ComponentProperty)itemId).getUri();
                else if (itemId instanceof CodeDatatypeTreeElement)
                    uri = ((CodeDatatypeTreeElement)itemId).getValue();
                else return null;
                
                // URI is 
                StringBuilder builder = new StringBuilder();
                builder.append("<h2>Properties of ");
                builder.append(uri);
                builder.append("</h2><br>");
                builder.append(generatePropertiesTable(uri, repoGraph));
                return builder.toString();
            }
        });
        
        repoTree.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (repoTree.hasChildren(target)) 
                    if (target instanceof Structure) {
                        return ACTIONS_DSD_NAVI; 
                    } else { 
                        return ACTIONS_NAVI;
                    }
                else return null;
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_EXPAND_ALL)
                    repoTree.expandItemsRecursively(target);
                else if (action == ACTION_COLLAPSE_ALL)
                    repoTree.collapseItemsRecursively(target);
                else if (action == ACTION_SET_AS_DSD) {
                    String dsd = ((Structure)target).getUri();
                    try {
                        RepositoryConnection conn = repository.getConnection();
                        for (String q: DSDRepoUtils.qCopyDSD(dsd, repoGraph, dataGraph)){
                            GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, q);
                            query.evaluate();
                        }
                        String qStructureProperty = "INSERT INTO GRAPH <" + dataGraph + "> { <" + dataset + "> <http://purl.org/linked-data/cube#structure> <" + dsd + "> }";
                        conn.prepareGraphQuery(QueryLanguage.SPARQL, qStructureProperty).evaluate();
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    }
                    getWindow().showNotification("Set " + dsd + " as qb:DataStructureDefinition");
                    dataTree.containerItemSetChange(null);
                    ds = new SparqlDataSet(repository, ds.getUri(), dataGraph);
                    findDSDs();
                }
            }
        });
        
        repoTree.setItemStyleGenerator(new Tree.ItemStyleGenerator() {
            public String getStyle(Object itemId) {
//                getWindow().showNotification("In the style");
//                if ((itemId instanceof ComponentProperty)){
//                    getWindow().showNotification("In the style 2");
//                    if (((ComponentProperty)itemId).getUri().equalsIgnoreCase(highlighted))
//                        return "highlight";
//                }
                return null;
            }
        });
    }
    
    private void populateCodeListTree (Collection<String> codeLists){
        compatibleCodeLists.removeAllItems();
        for (String cl: codeLists){
            compatibleCodeLists.addItem(cl);
            try {
                RepositoryConnection conn = repository.getConnection();
                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, DSDRepoUtils.qCodeListMemebers(cl, repoGraph));
                TupleQueryResult res = query.evaluate();
                while (res.hasNext()){
                    BindingSet set = res.next();
                    String code = set.getValue("code").stringValue();
                    compatibleCodeLists.addItem(code);
                    compatibleCodeLists.setParent(code, cl);
                    compatibleCodeLists.setChildrenAllowed(code, false);
                }
            } catch (RepositoryException ex) {
                Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
            } catch (MalformedQueryException ex) {
                Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
            } catch (QueryEvaluationException ex) {
                Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
            }
        }
    }
    
    private void refreshContentFindDSDs(DataSet ds){
        if (ds == null) {
            getWindow().showNotification("No dataset selected", Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        Structure struct = ds.getStructure();
        if (struct != null){
            contentLayout.addComponent(new Label("The dataset already has a DSD!"));
            return;
        }
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();;
        dataTree = new Tree("Dataset");
        dataTree.setWidth("500px");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        populateDataTree();
        addDataTreeListenersFind();
        contentLayout.addComponent(dataTree);
        contentLayout.setExpandRatio(dataTree, 0.0f);
        repoTree = new Tree("Matching Structures");
        repoTree.setNullSelectionAllowed(true);
        repoTree.setImmediate(true);
        repoTreeItems = new LinkedList<Structure>();
        populateRepoTree();
        VerticalLayout v = new VerticalLayout();
        contentLayout.addComponent(v);
        contentLayout.setExpandRatio(v, 2.0f);
        v.addComponent(repoTree);
        v.setExpandRatio(repoTree, 2.0f);
    }
    
    private void refreshContentStoreDSD(DataSet ds){
        if (ds == null) {
            getWindow().showNotification("No dataset selected", Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        Structure struct = ds.getStructure();
        if (struct == null){
            contentLayout.addComponent(new Label("The dataset doesn't contain a DSD!"));
            return;
        }
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();;
        dataTree = new Tree("Dataset");
        dataTree.setWidth("500px");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        populateStoreTree();
        addDataTreeListenersStore();
        contentLayout.addComponent(dataTree);
        contentLayout.setExpandRatio(dataTree, 0.0f);
        repoTree = new Tree("Matching Structures");
        repoTree.setNullSelectionAllowed(true);
        repoTree.setImmediate(true);
        repoTreeItems = new LinkedList<Structure>();
    }
    
    private void refreshContentCreateDSD(DataSet ds){
        if (ds == null) {
            getWindow().showNotification("No dataset selected", Window.Notification.TYPE_ERROR_MESSAGE);
            return;
        }
        Structure struct = ds.getStructure();
        if (struct != null){
            contentLayout.addComponent(new Label("The dataset already has a DSD!"));
            return;
        }
        
        dataset = ds.getUri();
        contentLayout.removeAllComponents();
        dataTree = new Tree("Dataset");
        dataTree.setNullSelectionAllowed(true);
        dataTree.setImmediate(true);
        dataTree.setWidth("500px");
        populateDataTree();
        addDataTreeListenersCreate();
        contentLayout.addComponent(dataTree);
        contentLayout.setExpandRatio(dataTree, 0.0f);
        
        final VerticalLayout right = new VerticalLayout();
        right.setSpacing(true);
        contentLayout.addComponent(right);
        contentLayout.setExpandRatio(right, 2.0f);
        lblUndefined = new Label("There are still x undefined components", Label.CONTENT_XHTML);
        right.addComponent(lblUndefined);
        lblMissingCodeLists = new Label("There are still y missing code lists", Label.CONTENT_XHTML);
        right.addComponent(lblMissingCodeLists);
        final TextField dsdUri = new TextField("Enter DSD URI");
        dsdUri.setWidth("300px");
        right.addComponent(dsdUri);
        final Button btnCreate = new Button("Create DSD");
        right.addComponent(btnCreate);
        right.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        compatibleCodeLists = new Tree("Compatible code lists");
        right.addComponent(compatibleCodeLists);
        
        updateUndefinedAndMissing();
        
        compatibleCodeLists.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (target == null) return null;
                if (compatibleCodeLists.getParent(target) != null) return null;
                return new Action [] { ACTION_SET_AS_CL };
            }
            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_SET_AS_CL){
                    Object item = compatibleCodeLists.getData();
                    if (item == null){
                        getWindow().showNotification("Error, the component cannot determine where to put the code list", Window.Notification.TYPE_ERROR_MESSAGE);
                        return;
                    }
                    if (dataTree.getChildren(item).size() == 2){
                        getWindow().showNotification("The component property already has a code list", Window.Notification.TYPE_ERROR_MESSAGE);
                        return;
                    }
                    try {
                        RepositoryConnection conn = repository.getConnection();
                        String cl = (String) target;
                        String prop = (String) dataTree.getValue();
                        GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, 
                                DSDRepoUtils.qPullCodeList(cl, prop, repoGraph, dataGraph));
                        query.evaluate();
                        getWindow().showNotification("Code List set");
                        addCodeListToDataTree();
                        updateUndefinedAndMissing();
                    } catch (RepositoryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (MalformedQueryException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    } catch (QueryEvaluationException ex) {
                        Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                        getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            }
        });
        btnCreate.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (numUndefinedComponents > 0){
                    getWindow().showNotification("There can be no undefined components", Window.Notification.TYPE_ERROR_MESSAGE);
                    return;
                }
                if (numMissingCodeLists > 0){
                    getWindow().showNotification("All code lists must first be created or imported", Window.Notification.TYPE_ERROR_MESSAGE);
                    return;
                }
                final String dsd = dsdUri.getValue().toString();
                if (!isUri(dsd)){
                    getWindow().showNotification("Enter a valid URI for the DSD", Window.Notification.TYPE_ERROR_MESSAGE);
                }
                
                try {
                    RepositoryConnection conn = repository.getConnection();
                    LinkedList<String> dList = new LinkedList<String>();
                    LinkedList<String> mList = new LinkedList<String>();
                    LinkedList<String> aList = new LinkedList<String>();
                    LinkedList<String> uList = new LinkedList<String>();
                    LinkedList<String> propList = new LinkedList<String>();
                    LinkedList<String> rangeList = new LinkedList<String>();
                    
                    for (Object id: dataTree.rootItemIds()){
                        Collection<?> children = dataTree.getChildren(id);
                        if (children == null) continue;
                        
                        Collection<String> list = null;
                        if (id.toString().startsWith("D")) list = dList;
                        else if (id.toString().startsWith("M")) list = mList;
                        else if (id.toString().startsWith("A")) list = aList;
                        else if (id.toString().startsWith("U")) list = uList;
                        
                        for (Object prop: dataTree.getChildren(id)){
                            CountingTreeHeader h = (CountingTreeHeader)dataTree.getChildren(prop).iterator().next();
                            propList.add(prop.toString());
                            list.add(prop.toString());
                            if (h.toString().startsWith("C")) {
                                rangeList.add("http://www.w3.org/2004/02/skos/core#Concept");
                            } else {
                                rangeList.add(dataTree.getChildren(h).iterator().next().toString());
                            }
                        }
                    }
                    if (uList.size() > 0){
                        getWindow().showNotification("There are undefined properties!", Window.Notification.TYPE_WARNING_MESSAGE);
                        return;
                    }
                    GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, 
                            DSDRepoUtils.qCreateDSD(dataset, dsd, dList, mList, aList, propList, rangeList, dataGraph));
                    query.evaluate();
                    getWindow().showNotification("DSD created!");
                    DSDRepoComponent.this.ds = new SparqlDataSet(repository, 
                            DSDRepoComponent.this.ds.getUri(), dataGraph);
                    createDSD();
                } catch (RepositoryException ex) {
                    Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                    getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                } catch (MalformedQueryException ex) {
                    Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                    getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                } catch (QueryEvaluationException ex) {
                    Logger.getLogger(DSDRepoComponent.class.getName()).log(Level.SEVERE, null, ex);
                    getWindow().showNotification(ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
                }
            }
        });
    }
    
    private boolean isUri(String uri){
        if (uri == null || uri.equals("")) return false;
        try {
            java.net.URI u = new java.net.URI(uri);
        } catch (URISyntaxException ex) {
            return false;
        }
        return true;
    }
    
    private void addCodeListToDataTree(){
        Object obj = compatibleCodeLists.rootItemIds().iterator().next();
        if (obj == null) return;
        Collection<?> children = compatibleCodeLists.getChildren(obj);
        if (children == null) return;
        Object dataTreeElement = compatibleCodeLists.getData();
        CountingTreeHeader codeListHeader = new CountingTreeHeader(dataTree, "Code List");
        dataTree.addItem(codeListHeader);
        dataTree.setParent(codeListHeader, dataTreeElement);
        for (Object child: children){
            CodeItem ci = new CodeItem(child.toString());
            dataTree.addItem(ci);
            dataTree.setParent(ci, codeListHeader);
        }
    }
    
}
