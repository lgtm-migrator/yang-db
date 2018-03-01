package com.kayhut.fuse.model.query.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class RelPropGroup extends BasePropGroup<RelProp> {
    public RelPropGroup() {}

    public RelPropGroup(RelProp...props) {
        super(props);
    }

    public RelPropGroup(Iterable<RelProp> props) {
        super(props);
    }

    @Override
    public RelPropGroup clone() {
        RelPropGroup propGroup = new RelPropGroup();
        propGroup.seteNum(geteNum());
        propGroup.props = new ArrayList<>(getProps());
        return propGroup;

    }

    public static RelPropGroup of(List<RelProp> props) {
        return new RelPropGroup(props);
    }
}
