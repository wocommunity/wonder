
package er.extensions.foundation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import junit.framework.Assert;

import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;

import er.erxtest.ERXTestCase;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXKey;

/** Tests of the public API of the ERXArrayUtilities class.
 *
 * This source file is automatically generated. The method names may be improved, and re-naming tests has no ill effect.
 * Feel free to add tests or change tests to demonstrate what should be the "contracted" behavior of the class.
 *
 * @author ray@ganymede.org, Ray Kiddy
 */
public class ERXArrayUtilitiesTest extends ERXTestCase {

    static NSMutableDictionary<String,String> one, two, three;
    static NSMutableArray<NSDictionary<String,String>> list1;
    static NSMutableArray<NSDictionary<String,String>> frankList1, bobList1, bobList2, extraList1;
    static NSMutableDictionary<String,NSArray<NSDictionary<String,String>>> map1, map2, map3, map4, map5, map6;
    static NSMutableDictionary<String,NSArray<String>> color1, color2, color3, color4, color5, color6;

    static NSArray<NSDictionary<String,String>> nullList;
    static String nullString;
    static ERXKey<String> nullERXKey;

    static EOQualifierEvaluation trueQualifierEvaluation = new EOQualifierEvaluation() {
        @Override
        public boolean evaluateWithObject(Object object) {
            return true;
        }
    };
    static EOQualifierEvaluation redQualifierEvaluation = new EOQualifierEvaluation() {
        @Override
        public boolean evaluateWithObject(Object object) {
            if (object instanceof String) {
                return ((String)object).equals("red");
            }
            return false;
        }
    };

    static {

        nullList = null;
        nullString = null;
        nullERXKey = null;

        one = new NSMutableDictionary<>();
        one.setObjectForKey("Bob", "firstName");
        one.setObjectForKey("Barker", "lastName");
        one.setObjectForKey("blue", "favoriteColor");

        two = new NSMutableDictionary<>();
        two.setObjectForKey("Bob", "firstName");
        two.setObjectForKey("red", "favoriteColor");

        three = new NSMutableDictionary<>();
        three.setObjectForKey("Frank", "firstName");
        three.setObjectForKey("Further", "lastName");
        three.setObjectForKey("green", "favoriteColor");

        list1 = new NSMutableArray<NSDictionary<String,String>>();
        list1.add(one);
        list1.add(two);
        list1.add(three);

        frankList1 = new NSMutableArray<NSDictionary<String,String>>();
        frankList1.add(three);

        bobList1 = new NSMutableArray<NSDictionary<String,String>>();
        bobList1.add(one);
        bobList1.add(two);

        bobList2 = new NSMutableArray<NSDictionary<String,String>>();
        bobList2.add(one);

        extraList1 = new NSMutableArray<NSDictionary<String,String>>();
        extraList1.add(two);

        // {
        //  Frank = ({lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; });
        //  Bob = ({lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; }, {firstName = "Bob"; favoriteColor = "red"; });
        // }
        //
        map1 = new NSMutableDictionary<String,NSArray<NSDictionary<String,String>>>();
        map1.setObjectForKey(frankList1, "Frank");
        map1.setObjectForKey(bobList1, "Bob");

        // {Frank = ("green"); Bob = ("blue", "red");
        //
        color1 = new NSMutableDictionary<String,NSArray<String>>();
        color1.setObjectForKey(new NSArray<>("green"), "Frank");
        color1.setObjectForKey(new NSArray<>(new String[] { "blue", "red" }), "Bob");

        // {
        //  Further = ({lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; });
        //  Barker = ({lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; });
        // }
        //
        map2 = new NSMutableDictionary<String,NSArray<NSDictionary<String,String>>>();
        map2.setObjectForKey(frankList1, "Further");
        map2.setObjectForKey(bobList2, "Barker");

        // {Further = ("green"); Barker = ("blue"); }
        //
        color2 = new NSMutableDictionary<String,NSArray<String>>();
        color2.setObjectForKey(new NSArray<>("green"), "Further");
        color2.setObjectForKey(new NSArray<>("blue"), "Barker");

        // {
        //  Further = ({lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; });
        //  Barker = ({lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; });
        //  **** NULL GROUPING KEY **** = ({firstName = "Bob"; favoriteColor = "red"; });
        // }
        //
        map3 = new NSMutableDictionary<String,NSArray<NSDictionary<String,String>>>();
        map3.setObjectForKey(frankList1, "Further");
        map3.setObjectForKey(bobList2, "Barker");
        map3.setObjectForKey(new NSArray<NSDictionary<String,String>>(two), ERXArrayUtilities.NULL_GROUPING_KEY);

        // {Further = ("green"); Barker = ("blue"); **** NULL GROUPING KEY **** = ("red"); }
        //
        color3 = new NSMutableDictionary<String,NSArray<String>>();
        color3.setObjectForKey(new NSArray<>("green"), "Further");
        color3.setObjectForKey(new NSArray<>("blue"), "Barker");
        color3.setObjectForKey(new NSArray<>("red"), ERXArrayUtilities.NULL_GROUPING_KEY);

        // {**** NULL GROUPING KEY **** = ("blue", "red", "green"); }
        //
        color4 = new NSMutableDictionary<String,NSArray<String>>();
        color4.setObjectForKey(new NSArray<>(new String[] { "blue", "red", "green" }), ERXArrayUtilities.NULL_GROUPING_KEY);

        // {
        //  **** NULL GROUPING KEY **** = (
        //        {lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; },
        //        {firstName = "Bob"; favoriteColor = "red"; },
        //        {lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; }
        //  );
        // }
        //
        map4 = new NSMutableDictionary<String,NSArray<NSDictionary<String,String>>>();
        NSMutableArray<NSDictionary<String,String>> objs = new NSMutableArray<NSDictionary<String,String>>();
        objs.add(one);
        objs.add(two);
        objs.add(three);
        map4.setObjectForKey(objs, ERXArrayUtilities.NULL_GROUPING_KEY);

        // {extra = ("blue", "red", "green"); }
        //
        color5 = new NSMutableDictionary<String,NSArray<String>>();
        color5.setObjectForKey(new NSArray<>(new String[] { "blue", "red", "green" }), "extra");

        //{extra = ("red"); Further = ("green"); Barker = ("blue"); }
        //
        color6 = new NSMutableDictionary<String,NSArray<String>>();
        color6.setObjectForKey(new NSArray<>("red"), "extra");
        color6.setObjectForKey(new NSArray<>("green"), "Further");
        color6.setObjectForKey(new NSArray<>("blue"), "Barker");

        // {
        //  extra = ({firstName = "Bob"; favoriteColor = "red"; });
        //  Further = ({lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; });
        //  Barker = ({lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; });
        // }
        map5 = new NSMutableDictionary<String,NSArray<NSDictionary<String,String>>>();
        map5.setObjectForKey(new NSArray<NSDictionary<String,String>>(one), "Barker");
        map5.setObjectForKey(new NSArray<NSDictionary<String,String>>(two), "extra");
        map5.setObjectForKey(new NSArray<NSDictionary<String,String>>(three), "Further");

        // {
        //  extra = (
        //           {lastName = "Barker"; firstName = "Bob"; favoriteColor = "blue"; },
        //           {firstName = "Bob"; favoriteColor = "red"; },
        //           {lastName = "Further"; firstName = "Frank"; favoriteColor = "green"; }
        //  );
        // }
        //
        map6 = new NSMutableDictionary<String,NSArray<NSDictionary<String,String>>>();
        map6.setObjectForKey(objs, "extra");
    }

    /* Tests that this method does create an NSSelector from a number of valid keys, and also tests that when there is a
     * "special" NSSelector instance to be found, that that NSSelector is returned.
     *
     * @see er.extensions.foundation.ERXArrayUtilities
     */
    public void testSortSelectorWithKey() {

        Assert.assertEquals(new NSSelector("random", ERXConstant.ObjectClassArray), ERXArrayUtilities.sortSelectorWithKey("random"));

        Assert.assertNull(ERXArrayUtilities.sortSelectorWithKey(""));

        Assert.assertNull(ERXArrayUtilities.sortSelectorWithKey(null));

        Assert.assertEquals(EOSortOrdering.CompareAscending, ERXArrayUtilities.sortSelectorWithKey("compareAscending"));

        // intended to be same as Assert.assertNotEquals of the above condition. Note that "equals" says the two selectors are equal
        // even when they are of different classes. The equals method was obviously overriden to do this. -rrk
        //
        Assert.assertEquals(new NSSelector("compareAscending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareAscending);
        Assert.assertTrue(! (new NSSelector("compareAscending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareAscending.getClass()));

        Assert.assertEquals(new NSSelector("compareCaseInsensitiveAscending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareCaseInsensitiveAscending);
        Assert.assertTrue(! (new NSSelector("compareCaseInsensitiveAscending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareCaseInsensitiveAscending.getClass()));

        Assert.assertEquals(new NSSelector("compareCaseInsensitiveDescending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareCaseInsensitiveDescending);
        Assert.assertTrue(! (new NSSelector("compareCaseInsensitiveDescending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareCaseInsensitiveDescending.getClass()));

        Assert.assertEquals(new NSSelector("compareDescending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareDescending);
        Assert.assertTrue(! (new NSSelector("compareDescending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareDescending.getClass()));
    }

    public void testArrayGroupedByKeyPathWithNSArrayString() {
        NSDictionary result013 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString);
        Assert.assertNull(result013);

        NSDictionary result029 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName");
        Assert.assertNull(result029);

        NSDictionary result045 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName");
        Assert.assertNull(result045);

        NSDictionary result109 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullString);
        Assert.assertEquals(map4,result109);

        NSDictionary result125 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName");
        Assert.assertEquals(map1,result125);

        NSDictionary result141 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName");
        Assert.assertEquals(map3,result141);
    }

    public void testArrayGroupedByKeyPathWithNSArrayERXKey() {

        NSDictionary result063 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey);
        Assert.assertNull(result063);

        NSDictionary result079 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"));
        Assert.assertNull(result079);

        NSDictionary result095 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"));
        Assert.assertNull(result095);

        NSDictionary result159 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey);
        Assert.assertEquals(map4,result159);

        NSDictionary result175 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"));
        Assert.assertEquals(map1,result175);

        NSDictionary result191 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"));
        Assert.assertEquals(map3,result191);
    }

    public void testArrayGroupedByKeyPathWithNSArrayStringObjectString() {

        NSDictionary result001 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, nullString, nullString);
        Assert.assertNull(result001);

        NSDictionary result002 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, nullString, "favoriteColor");
        Assert.assertNull(result002);

        NSDictionary result005 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, "extra", nullString);
        Assert.assertNull(result005);

        NSDictionary result006 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, "extra", "favoriteColor");
        Assert.assertNull(result006);

        NSDictionary result017 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", nullString, nullString);
        Assert.assertNull(result017);

        NSDictionary result018 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", nullString, "favoriteColor");
        Assert.assertNull(result018);

        NSDictionary result021 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", "extra", nullString);
        Assert.assertNull(result021);

        NSDictionary result022 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", "extra", "favoriteColor");
        Assert.assertNull(result022);

        NSDictionary result033 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", nullString, nullString);
        Assert.assertNull(result033);

        NSDictionary result034 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", nullString, "favoriteColor");
        Assert.assertNull(result034);

        NSDictionary result037 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", "extra", nullString);
        Assert.assertNull(result037);

        NSDictionary result038 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", "extra", "favoriteColor");
        Assert.assertNull(result038);

        NSDictionary result113 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", nullString, nullString);
        Assert.assertEquals(map1,result113);

        NSDictionary result114 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", nullString, "favoriteColor");
        Assert.assertEquals(color1,result114);

        NSDictionary result117 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", "extra", nullString);
        Assert.assertEquals(map1,result117);

        NSDictionary result118 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", "extra", "favoriteColor");
        Assert.assertEquals(color1,result118);

        NSDictionary result129 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", nullString, nullString);
        Assert.assertEquals(map2,result129);

        NSDictionary result130 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", nullString, "favoriteColor");
        Assert.assertEquals(color2,result130);

        NSDictionary result133 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", "extra", nullString);
        Assert.assertEquals(map5,result133);

        NSDictionary result134 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", "extra", "favoriteColor");
        Assert.assertEquals(color6,result134);

    }

    public void testArrayGroupedByKeyPathWithNSArrayStringBooleanString() {

        NSDictionary result009 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, false, nullString);
        Assert.assertNull(result009);

        NSDictionary result010 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, false, "favoriteColor");
        Assert.assertNull(result010);

        NSDictionary result013 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, true, nullString);
        Assert.assertNull(result013);

        NSDictionary result014 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullString, true, "favoriteColor");
        Assert.assertNull(result014);

        NSDictionary result025 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", false, nullString);
        Assert.assertNull(result025);

        NSDictionary result026 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", false, "favoriteColor");
        Assert.assertNull(result026);

        NSDictionary result029 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", true, nullString);
        Assert.assertNull(result029);

        NSDictionary result030 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "firstName", true, "favoriteColor");
        Assert.assertNull(result030);

        NSDictionary result041 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", false, nullString);
        Assert.assertNull(result041);

        NSDictionary result042 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", false, "favoriteColor");
        Assert.assertNull(result042);

        NSDictionary result045 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", true, nullString);
        Assert.assertNull(result045);

        NSDictionary result046 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, "lastName", true, "favoriteColor");
        Assert.assertNull(result046);

        NSDictionary result105 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullString, false, nullString);
        Assert.assertEquals(NSDictionary.EmptyDictionary,result105);

        NSDictionary result106 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullString, false, "favoriteColor");
        Assert.assertEquals(NSDictionary.EmptyDictionary,result106);

        NSDictionary result109 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullString, true, nullString);
        Assert.assertEquals(map4,result109);

        NSDictionary result110 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullString, true, "favoriteColor");
        Assert.assertEquals(color4,result110);

        NSDictionary result121 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", false, nullString);
        Assert.assertEquals(map1,result121);

        NSDictionary result122 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", false, "favoriteColor");
        Assert.assertEquals(color1,result122);

        NSDictionary result125 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", true, nullString);
        Assert.assertEquals(map1,result125);

        NSDictionary result126 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "firstName", true, "favoriteColor");
        Assert.assertEquals(color1,result126);

        NSDictionary result137 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", false, nullString);
        Assert.assertEquals(map2,result137);

        NSDictionary result138 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", false, "favoriteColor");
        Assert.assertEquals(color2,result138);

        NSDictionary result141 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", true, nullString);
        Assert.assertEquals(map3,result141);

        NSDictionary result142 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, "lastName", true, "favoriteColor");
        Assert.assertEquals(color3,result142);
    }

    public void testArrayGroupedByKeyPathWithNSArrayERXKeyObjectERXKey() {

        NSDictionary result051 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, nullString, nullERXKey);
        Assert.assertNull(result051);

        NSDictionary result052 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, nullString, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result052);

        NSDictionary result055 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, "extra", nullERXKey);
        Assert.assertNull(result055);

        NSDictionary result056 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, "extra", new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result056);

        NSDictionary result067 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), nullString, nullERXKey);
        Assert.assertNull(result067);

        NSDictionary result068 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), nullString, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result068);

        NSDictionary result071 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), "extra", nullERXKey);
        Assert.assertNull(result071);

        NSDictionary result072 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), "extra", new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result072);

        NSDictionary result083 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), nullString, nullERXKey);
        Assert.assertNull(result083);

        NSDictionary result084 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), nullString, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result084);

        NSDictionary result087 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), "extra", nullERXKey);
        Assert.assertNull(result087);

        NSDictionary result088 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), "extra", new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result088);

        NSDictionary result147 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, nullString, nullERXKey);
        Assert.assertEquals(NSDictionary.EmptyDictionary,result147);

        NSDictionary result148 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, nullString, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(NSDictionary.EmptyDictionary,result148);

        NSDictionary result151 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, "extra", nullERXKey);
        Assert.assertEquals(map6,result151);

        NSDictionary result152 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, "extra", new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color5,result152);

        NSDictionary result163 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), nullString, nullERXKey);
        Assert.assertEquals(map1,result163);

        NSDictionary result164 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), nullString, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color1,result164);

        NSDictionary result167 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), "extra", nullERXKey);
        Assert.assertEquals(map1,result167);

        NSDictionary result168 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), "extra", new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color1,result168);

        NSDictionary result179 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), nullString, nullERXKey);
        Assert.assertEquals(map2,result179);

        NSDictionary result180 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), nullString, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color2,result180);

        NSDictionary result183 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), "extra", nullERXKey);
        Assert.assertEquals(map5,result183);

        NSDictionary result184 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), "extra", new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color6,result184);
    }

    public void testArrayGroupedByKeyPathWithNSArrayERXKeyBooleanERXKey() {

        NSDictionary result059 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, false, nullERXKey);
        Assert.assertNull(result059);

        NSDictionary result060 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, false, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result060);

        NSDictionary result063 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, true, nullERXKey);
        Assert.assertNull(result063);

        NSDictionary result064 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, nullERXKey, true, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result064);

        NSDictionary result075 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), false, nullERXKey);
        Assert.assertNull(result075);

        NSDictionary result076 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), false, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result076);

        NSDictionary result079 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), true, nullERXKey);
        Assert.assertNull(result079);

        NSDictionary result080 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("firstName"), true, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result080);

        NSDictionary result091 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), false, nullERXKey);
        Assert.assertNull(result091);

        NSDictionary result092 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), false, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result092);

        NSDictionary result095 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), true, nullERXKey);
        Assert.assertNull(result095);

        NSDictionary result096 = ERXArrayUtilities.arrayGroupedByKeyPath(nullList, new ERXKey<String>("lastName"), true, new ERXKey<String>("favoriteColor"));
        Assert.assertNull(result096);

        NSDictionary result155 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, false, nullERXKey);
        Assert.assertEquals(NSDictionary.EmptyDictionary,result155);

        NSDictionary result156 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, false, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(NSDictionary.EmptyDictionary,result156);

        NSDictionary result159 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, true, nullERXKey);
        Assert.assertEquals(map4,result159);

        NSDictionary result160 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, nullERXKey, true, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color4,result160);

        NSDictionary result171 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), false, nullERXKey);
        Assert.assertEquals(map1,result171);

        NSDictionary result172 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), false, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color1,result172);

        NSDictionary result175 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), true, nullERXKey);
        Assert.assertEquals(map1,result175);

        NSDictionary result176 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("firstName"), true, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color1,result176);

        NSDictionary result187 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), false, nullERXKey);
        Assert.assertEquals(map2,result187);

        NSDictionary result188 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), false, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color2,result188);

        NSDictionary result191 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), true, nullERXKey);
        Assert.assertEquals(map3,result191);

        NSDictionary result192 = ERXArrayUtilities.arrayGroupedByKeyPath(list1, new ERXKey<String>("lastName"), true, new ERXKey<String>("favoriteColor"));
        Assert.assertEquals(color3,result192);
    }

    public void testArrayGroupedByToManyKeyPathERXKey() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, boolean);
    }

    public void testArrayGroupedByToManyKeyPathString() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, java.lang.String, boolean);
    }

    public void testArrayGroupedByToManyKeyPathERXKeyObject() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, java.lang.Object);
    }

    public void testArrayGroupedByToManyKeyPathStringObject() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.Object);
    }

    public void testArrayGroupedByToManyKeyPathERXKeyObjectERXKey() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, java.lang.Object, er.extensions.eof.ERXKey);
    }

    public void testArrayGroupedByToManyKeyPathStringObjectString() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.Object, java.lang.String);
    }

    public void testArraysAreIdenticalSets() {
        assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(null, null));
        assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<>(), new NSArray<>()));

        assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(null, new NSArray<>()));
        assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<>(), null));

        NSArray<String> set1 = new NSArray<>("red");
        NSArray<String> set2 = new NSArray<>("blue");

        assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<>("red"), set1));
        assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<>("red"), set2));

        NSArray<String> set3 = new NSArray<>("red", "blue");
        NSArray<String> set4 = new NSArray<>("blue", "red");

        assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(set3, set4));

        NSArray<String> set5 = new NSArray<>("blue", "red", "green");
        NSArray<String> set6 = new NSArray<>("green", "blue", "red");

        assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(set5, set6));
        assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(set5, set4));
    }

    public void testFilteredArrayWithQualifierEvaluation() {
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.filteredArrayWithQualifierEvaluation((NSArray)null, trueQualifierEvaluation));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.filteredArrayWithQualifierEvaluation(new NSArray<>(), trueQualifierEvaluation));

        NSArray<String> array1 = new NSArray<>("red");
        NSArray<String> array2 = new NSArray<>("red", "blue");

        assertEquals(array1, ERXArrayUtilities.filteredArrayWithQualifierEvaluation(array1, trueQualifierEvaluation));
        assertEquals(array2, ERXArrayUtilities.filteredArrayWithQualifierEvaluation(array2, trueQualifierEvaluation));

        assertEquals(array1, ERXArrayUtilities.filteredArrayWithQualifierEvaluation(array1, redQualifierEvaluation));
        assertEquals(array1, ERXArrayUtilities.filteredArrayWithQualifierEvaluation(array2, redQualifierEvaluation));

        List<String> array3 = Arrays.asList("green", "red", "blue");

        assertEquals(array1, ERXArrayUtilities.filteredArrayWithQualifierEvaluation(array3, redQualifierEvaluation));
    }

    public void testEnumerationHasMatchWithQualifierEvaluation() {
        NSArray<String> array1 = new NSArray<>("red");
        NSArray<String> array2 = new NSArray<>("red", "blue");
        NSArray<String> array3 = new NSArray<>("green", "blue");

        assertTrue(ERXArrayUtilities.enumerationHasMatchWithQualifierEvaluation(array1.objectEnumerator(), trueQualifierEvaluation));
        assertTrue(ERXArrayUtilities.enumerationHasMatchWithQualifierEvaluation(array2.objectEnumerator(), trueQualifierEvaluation));
        assertTrue(ERXArrayUtilities.enumerationHasMatchWithQualifierEvaluation(array3.objectEnumerator(), trueQualifierEvaluation));

        assertTrue(ERXArrayUtilities.enumerationHasMatchWithQualifierEvaluation(array1.objectEnumerator(), redQualifierEvaluation));
        assertTrue(ERXArrayUtilities.enumerationHasMatchWithQualifierEvaluation(array2.objectEnumerator(), redQualifierEvaluation));
        assertFalse(ERXArrayUtilities.enumerationHasMatchWithQualifierEvaluation(array3.objectEnumerator(), redQualifierEvaluation));
    }

    public void testIteratorHasMatchWithQualifierEvaluation() {
    	NSArray<String> array1 = new NSArray<>("red");
        NSArray<String> array2 = new NSArray<>("red", "blue");
        NSArray<String> array3 = new NSArray<>("green", "blue");

        assertTrue(ERXArrayUtilities.iteratorHasMatchWithQualifierEvaluation(array1.iterator(), trueQualifierEvaluation));
        assertTrue(ERXArrayUtilities.iteratorHasMatchWithQualifierEvaluation(array2.iterator(), trueQualifierEvaluation));
        assertTrue(ERXArrayUtilities.iteratorHasMatchWithQualifierEvaluation(array3.iterator(), trueQualifierEvaluation));

        assertTrue(ERXArrayUtilities.iteratorHasMatchWithQualifierEvaluation(array1.iterator(), redQualifierEvaluation));
        assertTrue(ERXArrayUtilities.iteratorHasMatchWithQualifierEvaluation(array2.iterator(), redQualifierEvaluation));
        assertFalse(ERXArrayUtilities.iteratorHasMatchWithQualifierEvaluation(array3.iterator(), redQualifierEvaluation));
    }

    public static class Person {
        private String name;
        private Integer age;

        public Person(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String name() {
            return name;
        }

        public Integer age() {
            return age;
        }

        public Object emptyAttribute() {
            return null;
        }
    }

    public void testArrayWithoutDuplicateKeyValue() {
        String ageKeyString = "age";
        ERXKey<Integer> ageKey = new ERXKey<>(ageKeyString);
        Person p1 = new Person("Adam", 20);
        Person p2 = new Person("Bertram", 20);
        Person p3 = new Person("Cassius", 25);
        Person p4 = new Person("Dorothe", 27);
        NSArray<Person> array1 = new NSArray<>(p1, p2);
        NSArray<Person> array2 = new NSArray<>(p1, p1, p1);
        NSArray<Person> array3 = new NSArray<>(p1, p3, p4);

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayWithoutDuplicateKeyValue(null, nullERXKey));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayWithoutDuplicateKeyValue(null, nullString));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayWithoutDuplicateKeyValue(new NSArray<>(), ageKeyString));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayWithoutDuplicateKeyValue(new ArrayList<>(), ageKeyString));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayWithoutDuplicateKeyValue(new NSArray<>(), ageKey));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayWithoutDuplicateKeyValue(new ArrayList<>(), ageKey));

        assertEquals(1, ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array1, ageKeyString).size());
        assertEquals(1, ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array2, ageKeyString).size());
        assertEquals(array3, ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array3, ageKeyString));
        assertEquals(1, ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array1, ageKey).size());
        assertEquals(1, ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array2, ageKey).size());
        assertEquals(array3, ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array3, ageKey));
    }

    public void testArrayMinusArray() {
        // TODO - null-safety in arrayMinusArray would be good.
        // assertNull(ERXArrayUtilities.arrayMinusArray(null, null));

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayMinusArray(new NSArray<>(), null));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayMinusArray(new NSArray<>(), new NSArray<>()));

        NSArray<String> array1 = new NSArray<>("red", "blue");
        NSArray<String> array2 = new NSArray<>("purple", "blue");
        NSArray<String> array3 = new NSArray<>("purple", "white");

        assertEquals(new NSArray<>("red") , ERXArrayUtilities.arrayMinusArray(array1, array2));
        assertEquals(array1, ERXArrayUtilities.arrayMinusArray(array1, array3));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayMinusArray(array1, array1));
        assertEquals(array3, ERXArrayUtilities.arrayMinusArray(array3, array1));
    }

    public void testArrayMinusObject() {
        // TODO - null-safety in arrayMinusObject would be good.
        // assertNull(ERXArrayUtilities.arrayMinusObject(null, null));
    	
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayMinusObject(new NSArray<>(), null));

        NSArray<String> array1 = new NSArray<>("red", "blue");
        NSArray<String> array2 = new NSArray<>("red", "blue", "red");

        assertEquals(array1, ERXArrayUtilities.arrayMinusObject(array1, null));

        assertEquals(array1, ERXArrayUtilities.arrayMinusObject(array1, "something"));
        assertEquals(new NSArray<>("red"), ERXArrayUtilities.arrayMinusObject(array1, "blue"));
        assertEquals(new NSArray<>("blue"), ERXArrayUtilities.arrayMinusObject(array1, "red"));

        assertEquals(2, ERXArrayUtilities.arrayMinusObject(array2, "blue").size());
        assertEquals(new NSArray<>("blue"), ERXArrayUtilities.arrayMinusObject(array2, "red"));
    }

    public void testArrayByAddingObjectsFromArrayWithoutDuplicates() {
        // TODO - null-safety in arrayByAddingObjectsFromArrayWithoutDuplicates would be good.
        // assertNull(ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(null, null));

        NSArray<String> array1 = new NSArray<>("red", "blue");
        NSArray<String> array2 = new NSArray<>("purple", "blue");
        NSArray<String> array3 = new NSArray<>("purple", "white");

        assertEquals(array1, ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, null));

        assertEquals(array1, ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, new NSArray<>()));
        assertEquals(array1, ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, array1));

        assertEquals(new NSArray<>("red", "blue", "purple"),
                            ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, array2));

        assertEquals(new NSArray<>("red", "blue", "purple", "white"),
                            ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, array3));

        assertEquals(new NSArray<>("purple", "blue", "red"),
                            ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array2, array1));
    }

    public void testArrayByRemovingFirstObject() {
        NSArray<String> immutableThree = new NSArray<>("one", "two", "three");
        NSArray<String> immutableTwo = new NSArray<>("two", "three");
        NSArray<String> immutableOne = new NSArray<>("three");

        assertEquals(immutableTwo, ERXArrayUtilities.arrayByRemovingFirstObject(immutableThree));
        assertEquals(immutableOne, ERXArrayUtilities.arrayByRemovingFirstObject(ERXArrayUtilities.arrayByRemovingFirstObject(immutableThree)));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayByRemovingFirstObject(
                                                   ERXArrayUtilities.arrayByRemovingFirstObject(ERXArrayUtilities.arrayByRemovingFirstObject(immutableThree))));

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayByRemovingFirstObject(NSArray.emptyArray()));
    }

    public void testSafeAddObject() {
        NSMutableArray<String> target = new NSMutableArray<>();
        NSArray<String> one = new NSArray<>("one");

        ERXArrayUtilities.safeAddObject(target, "one");
        assertEquals(one, target);

        String str = null;
        ERXArrayUtilities.safeAddObject(target, str);
        assertEquals(one, target);

        NSMutableArray<String> bad = null;
        ERXArrayUtilities.safeAddObject(bad, str);
        assertEquals(null, bad);
    }

    public void testAddObjectsFromArrayWithoutDuplicates() {
        NSMutableArray<String> first = new NSMutableArray<>("one", "two");
        NSMutableArray<String> second = new NSMutableArray<>("two", "one");

        NSArray<String> third = new NSArray<>("one", "three");
        NSArray<String> four = new NSArray<>("three", "one");

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(first, third);
        assertEquals(new NSArray<>("one", "two", "three"), first);

        first = new NSMutableArray<>("one", "two");

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(first, four);
        assertEquals(new NSArray<>("one", "two", "three"), first);

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(second, third);
        assertEquals(new NSArray<>("two", "one", "three"), second);

        second = new NSMutableArray<>("two", "one");

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(second, four);
        assertEquals(new NSArray<>("two", "one", "three"), second);

        second = new NSMutableArray<>("two", "one");
        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(second, null);
        assertEquals(new NSArray<>("two", "one"), second);
        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(second, new ArrayList<>());
        assertEquals(new NSArray<>("two", "one"), second);
    }

    public void testFlattenBoolean() {
        // public static com.webobjects.foundation.NSArray flatten(com.webobjects.foundation.NSArray, boolean);
    }

    public void testFlatten() {
        // public static com.webobjects.foundation.NSArray flatten(com.webobjects.foundation.NSArray);
    }

    public void testArrayFromPropertyList() {
        // public static com.webobjects.foundation.NSArray arrayFromPropertyList(java.lang.String, com.webobjects.foundation.NSBundle);
    }

    public void testValuesForKeyPaths() {
        Person p1 = new Person("Adam", 20);
        Person p2 = new Person("Bertram", 20);
        NSArray<Person> array1 = new NSArray<>(p1, p2);

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.valuesForKeyPaths(array1, null));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.valuesForKeyPaths(array1, new NSArray<>()));

        assertEquals(new NSArray<>(20, 20), ERXArrayUtilities.valuesForKeyPaths(array1, new NSArray<>("age")).get(0));
        assertEquals(new NSArray<>(NSKeyValueCoding.NullValue, NSKeyValueCoding.NullValue), ERXArrayUtilities.valuesForKeyPaths(array1, new NSArray<>("emptyAttribute")).get(0));
    }

    public void testFirstObject() {
        NSArray<String> array1 = new NSArray<>("one", "two", "three");
        List<String> array2 = Arrays.asList("one", "two", "three");

        assertNull(ERXArrayUtilities.firstObject(null));
        assertEquals("one", ERXArrayUtilities.firstObject(array1));
        assertEquals("one", ERXArrayUtilities.firstObject(array2));
    }

    public void testIndexOfFirstObjectWithValueForKeyPath() {
        String nameKeyString = "name";
        ERXKey<String> nameKey = new ERXKey<>(nameKeyString);
        Person p1 = new Person("Adam", 20);
        Person p2 = new Person("Bertram", 20);
        Person p3 = new Person("Cassius", 25);
        Person p4 = new Person("Dorothe", 27);
        NSArray<Person> array1 = new NSArray<>(p1, p2, p3, p4);

        assertEquals(-1, ERXArrayUtilities.indexOfFirstObjectWithValueForKeyPath(new NSArray<>(), null, nullERXKey));
        assertEquals(-1, ERXArrayUtilities.indexOfFirstObjectWithValueForKeyPath(new NSArray<>(), null, nullString));

        assertEquals(-1, ERXArrayUtilities.indexOfFirstObjectWithValueForKeyPath(array1, "xyz", nameKey));
        assertEquals(-1, ERXArrayUtilities.indexOfFirstObjectWithValueForKeyPath(array1, "xyz", nameKeyString));

        assertEquals(0, ERXArrayUtilities.indexOfFirstObjectWithValueForKeyPath(array1, "Adam", nameKeyString));
        assertEquals(1, ERXArrayUtilities.indexOfFirstObjectWithValueForKeyPath(array1, "Bertram", nameKeyString));
    }

    public void testFirstObjectWithValueForKeyPath() {
        String nameKeyString = "name";
        ERXKey<String> nameKey = new ERXKey<>(nameKeyString);
        Person p1 = new Person("Adam", 20);
        Person p2 = new Person("Bertram", 20);
        Person p3 = new Person("Cassius", 25);
        Person p4 = new Person("Dorothe", 27);
        NSArray<Person> array1 = new NSArray<>(p1, p2, p3, p4);

        assertNull(ERXArrayUtilities.firstObjectWithValueForKeyPath(new NSArray<>(), null, nullERXKey));
        assertNull(ERXArrayUtilities.firstObjectWithValueForKeyPath(new NSArray<>(), null, nullString));

        assertNull(ERXArrayUtilities.firstObjectWithValueForKeyPath(array1, "xyz", nameKey));
        assertNull(ERXArrayUtilities.firstObjectWithValueForKeyPath(array1, "xyz", nameKeyString));

        assertEquals(p1, ERXArrayUtilities.firstObjectWithValueForKeyPath(array1, "Adam", nameKeyString));
        assertEquals(p2, ERXArrayUtilities.firstObjectWithValueForKeyPath(array1, "Bertram", nameKeyString));
    }

    public void testObjectsWithValueForKeyPath() {
        String nameKeyString = "name";
        String ageKeyString = "age";
        ERXKey<String> nameKey = new ERXKey<>(nameKeyString);
        Person p1 = new Person("Adam", 20);
        Person p2 = new Person("Bertram", 20);
        Person p3 = new Person("Cassius", 25);
        Person p4 = new Person("Dorothe", 27);
        NSArray<Person> array1 = new NSArray<>(p1, p2, p3, p4);

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.objectsWithValueForKeyPath(null, null, nullERXKey));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.objectsWithValueForKeyPath(null, null, nullString));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.objectsWithValueForKeyPath(new NSArray<>(), null, nullERXKey));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.objectsWithValueForKeyPath(new NSArray<>(), null, nullString));

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.objectsWithValueForKeyPath(array1, "xyz", nameKeyString));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.objectsWithValueForKeyPath(array1, "xyz", nameKey));
        assertEquals(new NSArray<>(p3), ERXArrayUtilities.objectsWithValueForKeyPath(array1, "Cassius", nameKey));
        assertEquals(new NSArray<>(p1, p2), ERXArrayUtilities.objectsWithValueForKeyPath(array1, 20, ageKeyString));

        assertEquals(new NSArray<>(p1), ERXArrayUtilities.objectsWithValueForKeyPath(new NSArray<>(p1), null, "emptyAttribute"));
    }

    public void testIndexOfObjectUsingEqualator() {
        NSArray<String> array1 = new NSArray<>("red");
        NSArray<String> array2 = new NSArray<>("red", "blue");

        assertEquals(-1, ERXArrayUtilities.indexOfObjectUsingEqualator(new NSArray<>(), null, ERXEqualator.SafeEqualsEqualator));
        assertEquals(-1, ERXArrayUtilities.indexOfObjectUsingEqualator(array1, null, ERXEqualator.SafeEqualsEqualator));
        assertEquals(0, ERXArrayUtilities.indexOfObjectUsingEqualator(array1, "red", ERXEqualator.SafeEqualsEqualator));
        assertEquals(-1, ERXArrayUtilities.indexOfObjectUsingEqualator(array1, "blue", ERXEqualator.SafeEqualsEqualator));
        assertEquals(1, ERXArrayUtilities.indexOfObjectUsingEqualator(array2, "blue", ERXEqualator.SafeEqualsEqualator));
    }

    public void testSortedMutableArraySortedWithKey() {
        // public static com.webobjects.foundation.NSMutableArray sortedMutableArraySortedWithKey(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testSortedArraySortedWithKey() {
        // public static com.webobjects.foundation.NSArray sortedArraySortedWithKey(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testSortedArraySortedWithKeyNSSelector() {
        // public static com.webobjects.foundation.NSArray sortedArraySortedWithKey(com.webobjects.foundation.NSArray, java.lang.String, com.webobjects.foundation.NSSelector);
    }

    public void testSortedArraySortedWithKeys() {
        // public static com.webobjects.foundation.NSArray sortedArraySortedWithKeys(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray, com.webobjects.foundation.NSSelector);
    }

    public void testSortArrayWithKey() {
        // public static void sortArrayWithKey(com.webobjects.foundation.NSMutableArray, java.lang.String);
    }

    public void testSortArrayWithKeyNSSelector() {
        // public static void sortArrayWithKey(com.webobjects.foundation.NSMutableArray, java.lang.String, com.webobjects.foundation.NSSelector);
    }

    public void testMedian() {
        // public static java.lang.Number median(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testArrayWithoutDuplicates() {
        NSArray<String> array1 = new NSArray<>("one", "two", "three");
        NSArray<String> array2 = new NSArray<>("one", "two", "one", "four");
        List<String> array3 = Arrays.asList("one", "two", "one", "four");

        assertEquals(array1, ERXArrayUtilities.arrayWithoutDuplicates(array1));
        assertEquals(new NSArray<>("one", "two", "four"), ERXArrayUtilities.arrayWithoutDuplicates(array2));
        assertEquals(new NSArray<>("one", "two", "four"), ERXArrayUtilities.arrayWithoutDuplicates(array3));
    }

    public void testBatchedArrayWithSize() {
        NSArray<String> array1 = new NSArray<>("a", "b", "c", "d", "e", "f", "g", "h");

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.batchedArrayWithSize(null, 1));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.batchedArrayWithSize(new NSArray<>(), 1));

        try {
            ERXArrayUtilities.batchedArrayWithSize(array1, 0);
            fail("expected IllegalArgumentException for batchSize = 0");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        assertEquals(array1, ERXArrayUtilities.batchedArrayWithSize(array1, 100).get(0));
        assertEquals(array1.size(), ERXArrayUtilities.batchedArrayWithSize(array1, 1).size());

        NSArray<NSArray<String>> result = ERXArrayUtilities.batchedArrayWithSize(array1, 2);
        assertEquals(4, result.size());
        assertEquals(new NSArray<>("a", "b"), result.get(0));
    }

    public void testfilteredArrayWithEntityFetchSpecificationWithNSDictionary() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithEntityFetchSpecification(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, com.webobjects.foundation.NSDictionary);
    }

    public void testfilteredArrayWithEntityFetchSpecification() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithEntityFetchSpecification(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String);
    }

    public void testShiftObjectLeft() {
        NSMutableArray<String> array1 = new NSMutableArray<>("one", "two");
        NSArray<String> array2 = array1.immutableClone();

        ERXArrayUtilities.shiftObjectLeft(array1, "one");
        assertEquals(array2, array1);
        ERXArrayUtilities.shiftObjectLeft(array1, "three");
        assertEquals(array2, array1);

        ERXArrayUtilities.shiftObjectLeft(array1, "two");
        assertEquals(new NSArray<>("two", "one"), array1);
    }

    public void testShiftObjectRight() {
    	NSMutableArray<String> array1 = new NSMutableArray<>("one", "two");
        NSArray<String> array2 = array1.immutableClone();

        ERXArrayUtilities.shiftObjectRight(array1, "two");
        assertEquals(array2, array1);
        ERXArrayUtilities.shiftObjectRight(array1, "three");
        assertEquals(array2, array1);

        ERXArrayUtilities.shiftObjectRight(array1, "one");
        assertEquals(new NSArray<>("two", "one"), array1);
    }

    public void testArrayContainsAnyObjectFromArray() {
        NSArray<String> array1 = new NSArray<>("one", "two", "three", "four");
        NSArray<String> array2 = new NSArray<>("a", "b", "one", "c");
        NSArray<String> array3 = new NSArray<>("a", "b", "x", "c");
        List<String> array4 = Arrays.asList("1", "2", "one");

        assertFalse(ERXArrayUtilities.arrayContainsAnyObjectFromArray(null, null));
        assertFalse(ERXArrayUtilities.arrayContainsAnyObjectFromArray(new NSArray<>(), null));
        assertFalse(ERXArrayUtilities.arrayContainsAnyObjectFromArray(null, new NSArray<>()));
        assertFalse(ERXArrayUtilities.arrayContainsAnyObjectFromArray(new NSArray<>(), new NSArray<>()));

        assertTrue(ERXArrayUtilities.arrayContainsAnyObjectFromArray(array1, array1));
        assertTrue(ERXArrayUtilities.arrayContainsAnyObjectFromArray(array1, array2));
        assertFalse(ERXArrayUtilities.arrayContainsAnyObjectFromArray(array1, array3));
        assertTrue(ERXArrayUtilities.arrayContainsAnyObjectFromArray(array1, array4));
    }

    public void testArrayContainsArray() {
        NSArray<String> array1 = new NSArray<>("one", "two", "three", "four");
        NSArray<String> array2 = new NSArray<>("one", "two", "three");
        NSArray<String> array3 = new NSArray<>("1", "2", "3");

        assertFalse(ERXArrayUtilities.arrayContainsArray(null, null));
        assertFalse(ERXArrayUtilities.arrayContainsArray(new NSArray<>(), null));

        assertTrue(ERXArrayUtilities.arrayContainsArray(array1, null));
        assertTrue(ERXArrayUtilities.arrayContainsArray(array1, new NSArray<>()));

        assertTrue(ERXArrayUtilities.arrayContainsArray(array1, array1));
        assertTrue(ERXArrayUtilities.arrayContainsArray(array1, array2));
        assertFalse(ERXArrayUtilities.arrayContainsArray(array2, array1));
        assertFalse(ERXArrayUtilities.arrayContainsArray(array1, array3));
    }

    public void testIntersectingElements() {
        NSArray<String> array1 = new NSArray<>("one", "two", "three", "four");
        NSArray<String> array2 = new NSArray<>("one", "two", "three");
        NSArray<String> array3 = new NSArray<>("1", "2", "3");

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.intersectingElements(null, null));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.intersectingElements(array1, null));

        assertEquals(array2, ERXArrayUtilities.intersectingElements(array1, array2));
        assertEquals(new NSArray<>("2"), ERXArrayUtilities.intersectingElements(array3, new NSArray<>("2")));

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.intersectingElements(array1, new NSArray<>()));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.intersectingElements(array1, array3));
    }

    public void testReverse() {
        NSArray<String> array1 = new NSArray<>("1", "2", "3");
        List<Integer> array2 = IntStream.range(1, 100).boxed().collect(Collectors.toList());
        
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.reverse(null));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.reverse(new NSArray<>()));

        assertEquals(new NSArray<>("3", "2", "1"), ERXArrayUtilities.reverse(array1));
        assertEquals(new NSArray<>(99, 98, 97, 96, 95), ERXArrayUtilities.reverse(array2).subList(0, 5));
    }

    public void testfriendlyDisplayForKeyPath() {
        // public static java.lang.String friendlyDisplayForKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, java.lang.String, java.lang.String);
    }

    public void testArrayForKeysPath() {
        // public static com.webobjects.foundation.NSArray arrayForKeysPath(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testRemoveNullValues() {

        NSArray<Object> nullArray = null;
        NSArray<Object> first = new NSArray<>();
        NSArray<Object> second = new NSArray<>(NSKeyValueCoding.NullValue);
        NSArray<Object> third = new NSArray<>(new Object[] { "one", "two" });

        Assert.assertEquals(nullArray, ERXArrayUtilities.removeNullValues(nullArray));
        Assert.assertEquals(first, ERXArrayUtilities.removeNullValues(first));
        Assert.assertEquals(first, ERXArrayUtilities.removeNullValues(second));
        Assert.assertEquals(third, ERXArrayUtilities.removeNullValues(third));
    }

    public void testObjectLangArrayCastToStringArray() {

        String[] str1 = new String[] {};
        String[] str2 = ERXArrayUtilities.objectArrayCastToStringArray(new Object[] {});

        // TODO - When we are using junit 4.5, the org.junit.Assert class has methods for comparing language arrays directly, so do not use these NSArray instances.
        //
        Assert.assertEquals(new NSArray<>(str1), new NSArray<>(str2));

        String[] str3 = new String[] { "one" };
        String[] str4 = ERXArrayUtilities.objectArrayCastToStringArray(new Object[] { "one" });

        Assert.assertEquals(new NSArray<>(str3), new NSArray<>(str4));
    }

    public void testObjectLangArrayToString() {

        Assert.assertEquals("()", ERXArrayUtilities.objectArrayToString(new Object[] {}));
        Assert.assertEquals("(\"one\")", ERXArrayUtilities.objectArrayToString(new Object[] { "one" }));

        // TODO - Is there something else to do to test the objectArrayToString() method?
    }

    public void testObjectLangArrayOfLangArrayToString() {
        // public static java.lang.String objectArrayToString(java.lang.Object[][]);
    }

    public void testobjectArraysOfNSArraysToString() {
        // public static java.lang.String objectArraysToString(com.webobjects.foundation.NSArray);
    }

    public void testRemoveNullValuesFromEnd() {

        NSArray<Object> nullArray = null;
        NSArray<Object> first = new NSArray<>();
        NSArray<Object> second = new NSArray<>(NSKeyValueCoding.NullValue);
        NSArray<Object> third = new NSArray<>(new Object[] { "one", "two" });
        NSArray<Object> fourth = new NSArray<>(new Object[] { "one", "two", NSKeyValueCoding.NullValue });

        Assert.assertEquals(nullArray, ERXArrayUtilities.removeNullValuesFromEnd(nullArray));
        Assert.assertEquals(first, ERXArrayUtilities.removeNullValuesFromEnd(first));
        Assert.assertEquals(first, ERXArrayUtilities.removeNullValuesFromEnd(second));
        Assert.assertEquals(third, ERXArrayUtilities.removeNullValuesFromEnd(third));
        Assert.assertEquals(third, ERXArrayUtilities.removeNullValuesFromEnd(fourth));
    }

    public void testToStringArray() {
        String[] str1 = new String[] {};
        String[] str2 = ERXArrayUtilities.toStringArray(new NSArray<>());

        assertEquals(new NSArray<>(str1), new NSArray<>(str2));
    }

    public void testDictionaryOfObjectsIndexedByKeyPath() {
        assertEquals(NSDictionary.emptyDictionary(), ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(nullList, "name", true));
        assertEquals(NSDictionary.emptyDictionary(), ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(nullList, "name", false));
        assertEquals(NSDictionary.emptyDictionary(), ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(NSArray.emptyArray(), "name", true));
        assertEquals(NSDictionary.emptyDictionary(), ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(NSArray.emptyArray(), "name", false));

        NSMutableDictionary<String, String> dataOne = new NSMutableDictionary<>();
        dataOne.put("name", "Bob");
        dataOne.put("favoriteColor", "blue");

        NSMutableDictionary<String, String> dataTwo = new NSMutableDictionary<>();
        dataTwo.put("name", "Frank");
        dataTwo.put("favoriteColor", "green");

        NSMutableDictionary<String, String> dataThree = new NSMutableDictionary<>();
        dataThree.put("name", "Frank");
        dataThree.put("favoriteColor", "purple");

        NSMutableArray<NSDictionary<String, String>> array1 = new NSMutableArray<>(dataOne, dataTwo);

        NSDictionary<String, NSDictionary<String, String>> result1 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array1, "name", true);

        assertEquals(dataOne, result1.get("Bob"));
        assertEquals(dataTwo, result1.get("Frank"));
        assertEquals(new NSSet<>(new String[] { "Bob", "Frank" }), new NSSet<>(result1.allKeys()));

        NSDictionary<String, NSDictionary<String, String>> result2 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array1, "name", false);

        assertEquals(dataOne, result2.get("Bob"));
        assertEquals(dataTwo, result2.get("Frank"));
        assertEquals(new NSSet<>(new String[] { "Bob", "Frank" }), new NSSet<>(result2.allKeys()));

        NSMutableArray<NSDictionary<String, String>> array2 = new NSMutableArray<>(dataOne, dataTwo, dataThree);

        try {
            ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array2, "name", true);
            fail("expected RuntimeException due to key collision");
        } catch (RuntimeException re) {
            // test passed
        }

        NSDictionary<String, NSDictionary<String, String>> result4 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array2, "name", false);

        assertEquals(dataOne, result4.get("Bob"));
        assertEquals(dataThree, result4.get("Frank"));
        assertEquals(new NSSet<>(new String[] { "Bob", "Frank" }), new NSSet<>(result4.allKeys()));

        NSMutableDictionary<String, Object> job1 = new NSMutableDictionary<>();
        job1.put("jobTitle", "processor");
        job1.put("employee", dataOne);

        NSMutableDictionary<String, Object> job2 = new NSMutableDictionary<>();
        job2.put("jobTitle", "boss");
        job2.put("employee", dataTwo);

        NSMutableDictionary<String, Object> job3 = new NSMutableDictionary<>();
        job3.put("jobTitle", "flunky");
        job3.put("employee", dataThree);

        NSMutableArray<NSDictionary<String, Object>> array3 = new NSMutableArray<>(job1, job2);

        NSDictionary<String, NSDictionary<String, Object>> result5 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array3, "employee.name", true);

        assertEquals(job1, result5.get("Bob"));
        assertEquals(job2, result5.get("Frank"));
        assertEquals(new NSSet<>(new String[] { "Bob", "Frank" }), new NSSet<>(result5.allKeys()));

        NSDictionary<String, NSDictionary<String, Object>> result6 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array3, "employee.name", false);

        assertEquals(job1, result6.get("Bob"));
        assertEquals(job2, result6.get("Frank"));
        assertEquals(new NSSet<>(new String[] { "Bob", "Frank" }), new NSSet<>(result6.allKeys()));

        NSMutableArray<NSDictionary<String, Object>> array4 = new NSMutableArray<>(job1, job2, job3);

        try {
            ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array4, "employee.name", true);
            fail("expected RuntimeException due to key collision");
        } catch (RuntimeException re) {
            // test passed
        }

        NSDictionary<String, NSDictionary<String, Object>> result8 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPath(array4, "employee.name", false);

        assertEquals(job1, result8.get("Bob"));
        assertEquals(job3, result8.get("Frank"));
        assertEquals(new NSSet<>(new String[] { "Bob", "Frank" }), new NSSet<>(result8.allKeys()));
    }

    public void testArrayBySelectingInstancesOfClass() {
        Person p1 = new Person("Adam", 1);
        NSArray<String> array1 = new NSArray<>("one", "two", "three");
        NSArray<Object> array2 = new NSArray<>("one", 1, 2, p1);

        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayBySelectingInstancesOfClass(null, null));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayBySelectingInstancesOfClass(new NSArray<>(), null));

        assertEquals(array1, ERXArrayUtilities.arrayBySelectingInstancesOfClass(array1, null));
        assertEquals(array1, ERXArrayUtilities.arrayBySelectingInstancesOfClass(array1, String.class));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.arrayBySelectingInstancesOfClass(array1, Integer.class));

        assertEquals(new NSArray<>(1, 2), ERXArrayUtilities.arrayBySelectingInstancesOfClass(array2, Integer.class));
        assertEquals(new NSArray<>(p1), ERXArrayUtilities.arrayBySelectingInstancesOfClass(array2, Person.class));
    }

    public void testSortedArrayUsingComparator() {
        NSArray<String> array1 = new NSArray<>("c", "a", "d", "b");
        NSArray<String> array2 = new NSArray<>("a");
        NSArray<Object> array3 = new NSArray<>("c", 1, "d", "b");

        assertEquals(null, ERXArrayUtilities.sortedArrayUsingComparator(null, NSComparator.AscendingStringComparator));
        assertEquals(NSArray.emptyArray(), ERXArrayUtilities.sortedArrayUsingComparator(new NSArray<>(), NSComparator.AscendingStringComparator));

        assertEquals(new NSArray<>("a", "b", "c", "d"), ERXArrayUtilities.sortedArrayUsingComparator(array1, NSComparator.AscendingStringComparator));
        assertEquals(array2, ERXArrayUtilities.sortedArrayUsingComparator(array2, NSComparator.AscendingStringComparator));
        
        try {
            ERXArrayUtilities.sortedArrayUsingComparator(array3, NSComparator.AscendingStringComparator);
            fail("expected RuntimeException");
        } catch (RuntimeException e) {
            // test passed
        }
    }

    public void testSwapObjectsMutableArrayWithIndexes() {
        NSMutableArray<String> nullArray = null;
        NSMutableArray<String> emptyArray = new NSMutableArray<>();
        NSMutableArray<String> array = new NSMutableArray<>("a", "b");

        try {
            ERXArrayUtilities.swapObjects(nullArray, 0, 0);
            fail("expected IllegalArgumentException for null array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(emptyArray, 0, 0);
            fail("expected IllegalArgumentException for empty array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, -1, 0);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, 0, -1);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, array.size(), 0);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, 0, array.size());
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ERXArrayUtilities.swapObjects(array, 0, 0);
        assertEquals(array, array);

        ERXArrayUtilities.swapObjects(array, 0, 1);
        assertEquals("b", array.get(0));
        assertEquals("a", array.get(1));
    }

    public void testSwapObjectsMutableArrayWithObjectAndIndex() {
        NSMutableArray<String> nullArray = null;
        NSMutableArray<String> emptyArray = new NSMutableArray<>();
        String objectA = "a";
        String unknownObject = "d";
        NSMutableArray<String> array = new NSMutableArray<>(objectA, "b");

        try {
            ERXArrayUtilities.swapObjects(nullArray, objectA, 0);
            fail("expected IllegalArgumentException for null array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(emptyArray, objectA, 0);
            fail("expected IllegalArgumentException for empty array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, unknownObject, 0);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, objectA, -1);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, objectA, array.size());
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ERXArrayUtilities.swapObjects(array, objectA, 0);
        assertEquals(array, array);

        ERXArrayUtilities.swapObjects(array, objectA, 1);
        assertEquals("b", array.get(0));
        assertEquals(objectA, array.get(1));
    }

    public void testSwapObjectsMutableArrayWithObjects() {
        NSMutableArray<String> nullArray = null;
        NSMutableArray<String> emptyArray = new NSMutableArray<>();
        String objectA = "a";
        String objectB = "b";
        String unknownObject = "d";
        NSMutableArray<String> array = new NSMutableArray<>(objectA, objectB);

        try {
            ERXArrayUtilities.swapObjects(nullArray, objectA, objectB);
            fail("expected IllegalArgumentException for null array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(emptyArray, objectA, objectB);
            fail("expected IllegalArgumentException for empty array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, objectA, unknownObject);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, unknownObject, objectB);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        ERXArrayUtilities.swapObjects(array, objectA, objectA);
        assertEquals(array, array);

        ERXArrayUtilities.swapObjects(array, objectA, objectB);
        assertEquals(objectB, array.get(0));
        assertEquals(objectA, array.get(1));
    }

    public void testSwapObjectsArrayWithIndexes() {
        NSArray<String> nullArray = null;
        NSArray<String> emptyArray = new NSArray<>();
        NSArray<String> array = new NSArray<>("a", "b");

        try {
            ERXArrayUtilities.swapObjects(nullArray, 0, 0);
            fail("expected IllegalArgumentException for null array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(emptyArray, 0, 0);
            fail("expected IllegalArgumentException for empty array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, -1, 0);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, 0, -1);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, array.size(), 0);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, 0, array.size());
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        NSArray<String> result = ERXArrayUtilities.swapObjects(array, 0, 0);
        assertEquals(array, result);

        result = ERXArrayUtilities.swapObjects(array, 0, 1);
        assertEquals("b", result.get(0));
        assertEquals("a", result.get(1));
    }

    public void testSwapObjectsArrayWithObjectAndIndex() {
        NSArray<String> nullArray = null;
        NSArray<String> emptyArray = new NSArray<>();
        String objectA = "a";
        String unknownObject = "d";
        NSArray<String> array = new NSArray<>(objectA, "b");

        try {
            ERXArrayUtilities.swapObjects(nullArray, objectA, 0);
            fail("expected IllegalArgumentException for null array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(emptyArray, objectA, 0);
            fail("expected IllegalArgumentException for empty array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, unknownObject, 0);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, objectA, -1);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, objectA, array.size());
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        NSArray<String> result = ERXArrayUtilities.swapObjects(array, objectA, 0);
        assertEquals(array, result);

        result = ERXArrayUtilities.swapObjects(array, objectA, 1);
        assertEquals("b", result.get(0));
        assertEquals(objectA, result.get(1));
    }

    public void testSwapObjectsArrayWithObjects() {
        NSArray<String> nullArray = null;
        NSArray<String> emptyArray = new NSArray<>();
        String objectA = "a";
        String objectB = "b";
        String unknownObject = "d";
        NSArray<String> array = new NSArray<>(objectA, objectB);

        try {
            ERXArrayUtilities.swapObjects(nullArray, objectA, objectB);
            fail("expected IllegalArgumentException for null array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(emptyArray, objectA, objectB);
            fail("expected IllegalArgumentException for empty array");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, objectA, unknownObject);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        try {
            ERXArrayUtilities.swapObjects(array, unknownObject, objectB);
            fail("expected IllegalArgumentException for out of bound index");
        } catch (IllegalArgumentException e) {
            // test passed
        }

        NSArray<String> result = ERXArrayUtilities.swapObjects(array, objectA, objectA);
        assertEquals(array, result);

        result = ERXArrayUtilities.swapObjects(array, objectA, objectB);
        assertEquals(objectB, result.get(0));
        assertEquals(objectA, result.get(1));
    }

    public void testDeepClone() {
        // public static com.webobjects.foundation.NSArray deepClone(com.webobjects.foundation.NSArray, boolean);
    }

    public void testArrayIsNullOrEmpty() {
        assertTrue(ERXArrayUtilities.arrayIsNullOrEmpty(null));
        assertTrue(ERXArrayUtilities.arrayIsNullOrEmpty(new NSArray<>()));
        assertTrue(ERXArrayUtilities.arrayIsNullOrEmpty(new ArrayList<>()));
        assertFalse(ERXArrayUtilities.arrayIsNullOrEmpty(new NSArray<>("1")));
        assertFalse(ERXArrayUtilities.arrayIsNullOrEmpty(Arrays.asList("1")));
    }

    public void testStdDev() {
    	String numKey = "num";
    	NSDictionary<String, Integer> uno = new NSDictionary<>(Integer.valueOf(1), numKey);
    	NSDictionary<String, Integer> dos = new NSDictionary<>(Integer.valueOf(2), numKey);
    	NSDictionary<String, Integer> tres = new NSDictionary<>(Integer.valueOf(3), numKey);
    	NSDictionary<String, Integer> quatro = new NSDictionary<>(Integer.valueOf(4), numKey);
    	NSDictionary<String, Integer> cinco = new NSDictionary<>(Integer.valueOf(5), numKey);
    	NSArray<NSDictionary<String, Integer>> numbers = new NSArray<NSDictionary<String,Integer>>(uno, dos, tres, quatro, cinco);
    	BigDecimal pop = ERXValueUtilities.bigDecimalValue(ERXArrayUtilities.stdDev(numbers, numKey, true));
    	assertTrue(BigDecimal.valueOf(Math.sqrt(2)).compareTo(pop) == 0);
    	BigDecimal samp = ERXValueUtilities.bigDecimalValue(ERXArrayUtilities.stdDev(numbers, numKey, false));
    	assertTrue(BigDecimal.valueOf(Math.sqrt(2.5)).compareTo(samp) == 0);
    }
}
