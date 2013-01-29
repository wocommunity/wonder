
package er.extensions.foundation;

import java.math.BigDecimal;

import junit.framework.Assert;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
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

    static {

        nullList = null;
        nullString = null;
        nullERXKey = null;

        one = new NSMutableDictionary<String,String>();
        one.setObjectForKey("Bob", "firstName");
        one.setObjectForKey("Barker", "lastName");
        one.setObjectForKey("blue", "favoriteColor");

        two = new NSMutableDictionary<String,String>();
        two.setObjectForKey("Bob", "firstName");
        two.setObjectForKey("red", "favoriteColor");

        three = new NSMutableDictionary<String,String>();
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
        color1.setObjectForKey(new NSArray<String>("green"), "Frank");
        color1.setObjectForKey(new NSArray<String>(new String[] { "blue", "red" }), "Bob");

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
        color2.setObjectForKey(new NSArray<String>("green"), "Further");
        color2.setObjectForKey(new NSArray<String>("blue"), "Barker");

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
        color3.setObjectForKey(new NSArray<String>("green"), "Further");
        color3.setObjectForKey(new NSArray<String>("blue"), "Barker");
        color3.setObjectForKey(new NSArray<String>("red"), ERXArrayUtilities.NULL_GROUPING_KEY);

        // {**** NULL GROUPING KEY **** = ("blue", "red", "green"); }
        //
        color4 = new NSMutableDictionary<String,NSArray<String>>();
        color4.setObjectForKey(new NSArray<String>(new String[] { "blue", "red", "green" }), ERXArrayUtilities.NULL_GROUPING_KEY);

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
        color5.setObjectForKey(new NSArray<String>(new String[] { "blue", "red", "green" }), "extra");

        //{extra = ("red"); Further = ("green"); Barker = ("blue"); }
        //
        color6 = new NSMutableDictionary<String,NSArray<String>>();
        color6.setObjectForKey(new NSArray<String>("red"), "extra");
        color6.setObjectForKey(new NSArray<String>("green"), "Further");
        color6.setObjectForKey(new NSArray<String>("blue"), "Barker");

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

    public void testSetFromArray() {
        // public static com.webobjects.foundation.NSSet setFromArray(com.webobjects.foundation.NSArray);
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

        Assert.assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(null, null));
        Assert.assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<Object>(), new NSArray<Object>()));

        Assert.assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(null, new NSArray<Object>()));
        Assert.assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<Object>(), null));

        NSArray<String> set1 = new NSArray<String>("red");
        NSArray<String> set2 = new NSArray<String>("blue");

        Assert.assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<String>("red"), set1));
        Assert.assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(new NSArray<String>("red"), set2));

        NSArray<String> set3 = new NSArray<String>(new String[] { "red", "blue" });
        NSArray<String> set4 = new NSArray<String>(new String[] { "blue", "red" });

        Assert.assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(set3, set4));

        NSArray<String> set5 = new NSArray<String>(new String[] { "blue", "red", "green" });
        NSArray<String> set6 = new NSArray<String>(new String[] { "green", "blue", "red" });

        Assert.assertTrue(ERXArrayUtilities.arraysAreIdenticalSets(set5, set6));
        Assert.assertFalse(ERXArrayUtilities.arraysAreIdenticalSets(set5, set4));
    }

    public void testfilteredArrayWithQualifierEvaluationNSArray() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithQualifierEvaluation(com.webobjects.foundation.NSArray, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testfilteredArrayWithQualifierEvaluationEnumeration() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithQualifierEvaluation(java.util.Enumeration, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testenumerationHasMatchWithQualifierEvaluation() {
        // public static boolean enumerationHasMatchWithQualifierEvaluation(java.util.Enumeration, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testiteratorHasMatchWithQualifierEvaluation() {
        // public static boolean iteratorHasMatchWithQualifierEvaluation(java.util.Iterator, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testfilteredArrayWithQualifierEvaluation() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithQualifierEvaluation(java.util.Iterator, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testArrayWithoutDuplicateKeyValue() {
        // public static com.webobjects.foundation.NSArray arrayWithoutDuplicateKeyValue(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testArrayMinusArray() {

        // TODO - null-safety in arrayMinusArray would be good.

        // Assert.assertNull(ERXArrayUtilities.arrayMinusArray(null, null));
        // Assert.assertEquals(new NSArray<Object>(), ERXArrayUtilities.arrayMinusArray(new NSArray<Object>(), null));

        Assert.assertEquals(new NSArray<Object>(), ERXArrayUtilities.arrayMinusArray(new NSArray<Object>(), new NSArray<Object>()));

        NSArray<String> array1 = new NSArray<String>(new String[] { "red", "blue" });
        NSArray<String> array2 = new NSArray<String>(new String[] { "purple", "blue" });
        NSArray<String> array3 = new NSArray<String>(new String[] { "purple", "white" });

        Assert.assertEquals(new NSArray<String>("red") , ERXArrayUtilities.arrayMinusArray(array1, array2));
        Assert.assertEquals(array1, ERXArrayUtilities.arrayMinusArray(array1, array3));
        Assert.assertEquals(new NSArray<String>(), ERXArrayUtilities.arrayMinusArray(array1, array1));
        Assert.assertEquals(array3, ERXArrayUtilities.arrayMinusArray(array3, array1));
    }

    public void testArrayMinusObject() {

        // TODO - null-safety in arrayMinusObject would be good.

        //Assert.assertNull(ERXArrayUtilities.arrayMinusObject(null, null));
        //Assert.assertEquals(new NSArray<String>(), ERXArrayUtilities.arrayMinusObject(new NSArray<String>(), null));

        NSArray<String> array1 = new NSArray<String>(new String[] { "red", "blue" });

        //Assert.assertEquals(array1, ERXArrayUtilities.arrayMinusObject(array1, null));

        Assert.assertEquals(array1, ERXArrayUtilities.arrayMinusObject(array1, "something"));
        Assert.assertEquals(new NSArray<String>("red"), ERXArrayUtilities.arrayMinusObject(array1, "blue"));
        Assert.assertEquals(new NSArray<String>("blue"), ERXArrayUtilities.arrayMinusObject(array1, "red"));
    }

    public void testArrayByAddingObjectsFromArrayWithoutDuplicates() {

        // TODO - null-safety in arrayByAddingObjectsFromArrayWithoutDuplicates would be good.

        //Assert.assertNull(ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(null, null));

        NSArray<String> array1 = new NSArray<String>(new String[] { "red", "blue" });
        NSArray<String> array2 = new NSArray<String>(new String[] { "purple", "blue" });
        NSArray<String> array3 = new NSArray<String>(new String[] { "purple", "white" });

        //Assert.assertEquals(array1, ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, null));

        Assert.assertEquals(array1, ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, new NSArray<String>()));
        Assert.assertEquals(array1, ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, array1));

        Assert.assertEquals(new NSArray<String>(new String[] { "red", "blue", "purple" }),
                            ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, array2));

        Assert.assertEquals(new NSArray<String>(new String[] { "red", "blue", "purple", "white" }),
                            ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array1, array3));

        Assert.assertEquals(new NSArray<String>(new String[] { "purple", "blue", "red" }),
                            ERXArrayUtilities.arrayByAddingObjectsFromArrayWithoutDuplicates(array2, array1));
    }

    public void testArrayByRemovingFirstObject() {

        NSArray<String> immutableThree = new NSArray<String>(new String[] { "one", "two", "three" });

		NSArray<String> immutableTwo = new NSArray<String>(new String[] { "two", "three" });

		NSArray<String> immutableOne = new NSArray<String>(new String[] { "three" });

        Assert.assertEquals(immutableTwo, ERXArrayUtilities.arrayByRemovingFirstObject(immutableThree));
        Assert.assertEquals(immutableOne, ERXArrayUtilities.arrayByRemovingFirstObject(ERXArrayUtilities.arrayByRemovingFirstObject(immutableThree)));
        Assert.assertEquals(new NSArray(), ERXArrayUtilities.arrayByRemovingFirstObject(
                                                   ERXArrayUtilities.arrayByRemovingFirstObject(ERXArrayUtilities.arrayByRemovingFirstObject(immutableThree))));

        Assert.assertEquals(new NSArray<Object>(), ERXArrayUtilities.arrayByRemovingFirstObject(new NSArray<Object>()));
        Assert.assertEquals(null, null);
    }

    public void testSafeAddObject() {
        NSMutableArray<String> target = new NSMutableArray<String>();

        ERXArrayUtilities.safeAddObject(target, "one");
        Assert.assertEquals(new NSMutableArray<String>("one"), target);

        String str = null;
        ERXArrayUtilities.safeAddObject(target, str);
        Assert.assertEquals(new NSMutableArray<String>("one"), target);

        NSMutableArray<String> bad = null;

        ERXArrayUtilities.safeAddObject(bad, str);
    }

    public void testAddObjectsFromArrayWithoutDuplicates() {

        NSMutableArray<String> first = new NSMutableArray<String>(new String[] { "one", "two" });
        NSMutableArray<String> second = new NSMutableArray<String>(new String[] { "two", "one" });

        NSArray<String> third = new NSArray<String>(new String[] { "one", "three" });
        NSArray<String> four = new NSArray<String>(new String[] { "three", "one" });

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(first, third);
        Assert.assertEquals(new NSMutableArray<String>(new String[] { "one", "two", "three" }), first);

        first = new NSMutableArray<String>(new String[] { "one", "two" });

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(first, four);
        Assert.assertEquals(new NSMutableArray<String>(new String[] { "one", "two", "three" }), first);

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(second, third);
        Assert.assertEquals(new NSMutableArray<String>(new String[] { "two", "one", "three" }), second);

        second = new NSMutableArray<String>(new String[] { "two", "one" });

        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(second, four);
        Assert.assertEquals(new NSMutableArray<String>(new String[] { "two", "one", "three" }), second);
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

    public void testvaluesForKeyPaths() {
        // public static com.webobjects.foundation.NSArray valuesForKeyPaths(java.lang.Object, com.webobjects.foundation.NSArray);
    }

    public void testfirstObject() {
        // public static java.lang.Object firstObject(com.webobjects.foundation.NSArray);
    }

    public void testindexOfFirstObjectWithValueForKeyPath() {
        // public static int indexOfFirstObjectWithValueForKeyPath(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.String);
    }

    public void testfirstObjectWithValueForKeyPath() {
        // public static java.lang.Object firstObjectWithValueForKeyPath(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.String);
    }

    public void testobjectsWithValueForKeyPath() {
        // public static com.webobjects.foundation.NSArray objectsWithValueForKeyPath(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.String);
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

    public void testInitialize() {
        // public static void initialize();
    }

    public void testMedian() {
        // public static java.lang.Number median(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testDistinct() {
        // public static com.webobjects.foundation.NSArray distinct(com.webobjects.foundation.NSArray);
    }

    public void testArrayWithoutDuplicates() {
        // public static com.webobjects.foundation.NSArray arrayWithoutDuplicates(com.webobjects.foundation.NSArray);
    }

    public void testbatchedArrayWithSize() {
        // public static com.webobjects.foundation.NSArray batchedArrayWithSize(com.webobjects.foundation.NSArray, int);
    }

    public void testfilteredArrayWithEntityFetchSpecificationWithNSDictionary() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithEntityFetchSpecification(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, com.webobjects.foundation.NSDictionary);
    }

    public void testfilteredArrayWithFetchSpecificationNamedEntityNamedBindings() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithFetchSpecificationNamedEntityNamedBindings(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, com.webobjects.foundation.NSDictionary);
    }

    public void testfilteredArrayWithFetchSpecificationNamedEntityNamed() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithFetchSpecificationNamedEntityNamed(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String);
    }

    public void testfilteredArrayWithEntityFetchSpecification() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithEntityFetchSpecification(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String);
    }

    public void testShiftObjectLeft() {
        // public static void shiftObjectLeft(com.webobjects.foundation.NSMutableArray, java.lang.Object);
    }

    public void testShiftObjectRight() {
        // public static void shiftObjectRight(com.webobjects.foundation.NSMutableArray, java.lang.Object);
    }

    public void testArrayContainsAnyObjectFromArray() {
        // public static boolean arrayContainsAnyObjectFromArray(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testArrayContainsArray() {
        // public static boolean arrayContainsArray(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testintersectingElements() {
        // public static com.webobjects.foundation.NSArray intersectingElements(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testreverse() {
        // public static com.webobjects.foundation.NSArray reverse(com.webobjects.foundation.NSArray);
    }

    public void testfriendlyDisplayForKeyPath() {
        // public static java.lang.String friendlyDisplayForKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, java.lang.String, java.lang.String);
    }

    public void testArrayForKeysPath() {
        // public static com.webobjects.foundation.NSArray arrayForKeysPath(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testRemoveNullValues() {

        NSArray<Object> nullArray = null;
        NSArray<Object> first = new NSArray<Object>();
        NSArray<Object> second = new NSArray<Object>(NSKeyValueCoding.NullValue);
        NSArray<Object> third = new NSArray<Object>(new Object[] { "one", "two" });

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
        Assert.assertEquals(new NSArray<String>(str1), new NSArray<String>(str2));

        String[] str3 = new String[] { "one" };
        String[] str4 = ERXArrayUtilities.objectArrayCastToStringArray(new Object[] { "one" });

        Assert.assertEquals(new NSArray<String>(str3), new NSArray<String>(str4));
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
        NSArray<Object> first = new NSArray<Object>();
        NSArray<Object> second = new NSArray<Object>(NSKeyValueCoding.NullValue);
        NSArray<Object> third = new NSArray<Object>(new Object[] { "one", "two" });
        NSArray<Object> fourth = new NSArray<Object>(new Object[] { "one", "two", NSKeyValueCoding.NullValue });

        Assert.assertEquals(nullArray, ERXArrayUtilities.removeNullValuesFromEnd(nullArray));
        Assert.assertEquals(first, ERXArrayUtilities.removeNullValuesFromEnd(first));
        Assert.assertEquals(first, ERXArrayUtilities.removeNullValuesFromEnd(second));
        Assert.assertEquals(third, ERXArrayUtilities.removeNullValuesFromEnd(third));
        Assert.assertEquals(third, ERXArrayUtilities.removeNullValuesFromEnd(fourth));
    }

    public void testToStringArray() {
        String[] str1 = new String[] {};
        String[] str2 = ERXArrayUtilities.toStringArray(new NSArray<Object>());

        Assert.assertEquals(new NSArray<String>(str1), new NSArray<String>(str2));
    }

    public void testDictionaryOfObjectsIndexedByKeyPath() {
        // Does nothing but call testDictionaryOfObjectsIndexedByKeyPathThrowOnCollision with throwsOnCollision = false, and
        // so can test those cases in testDictionaryOfObjectsIndexedByKeyPathThrowOnCollision().
    }

    // TODO Why is this failing for me?
    //
    public void _testDictionaryOfObjectsIndexedByKeyPathThrowOnCollision() {

        //Assert.assertEquals(NSDictionary.EmptyDictionary, ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(nullList, "name", true));
        //Assert.assertEquals(NSDictionary.EmptyDictionary, ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(nullList, "name", false));

        NSMutableDictionary<String,String> dataOne = new NSMutableDictionary<String,String>();
        one.setObjectForKey("Bob", "name");
        one.setObjectForKey("blue", "favoriteColor");

        NSMutableDictionary<String,String> dataTwo = new NSMutableDictionary<String,String>();
        two.setObjectForKey("Frank", "name");
        two.setObjectForKey("green", "favoriteColor");

        NSMutableDictionary<String,String> dataThree = new NSMutableDictionary<String,String>();
        three.setObjectForKey("Frank", "name");
        three.setObjectForKey("purple", "favoriteColor");

        NSMutableArray<NSDictionary<String,String>> listOne = new NSMutableArray<NSDictionary<String,String>>();
        listOne.add(dataOne);
        listOne.add(dataTwo);

        NSDictionary<String,NSDictionary<String,String>> result1 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list1, "name", true);

        Assert.assertEquals(one, result1.objectForKey("Bob"));
        Assert.assertEquals(two, result1.objectForKey("Frank"));
        Assert.assertEquals(new NSSet<String>(new String[] { "Bob", "Frank" }), new NSSet<String>(result1.allKeys()));

        NSDictionary<String,NSDictionary<String,String>> result2 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list1, "name", false);

        Assert.assertEquals(dataOne, result2.objectForKey("Bob"));
        Assert.assertEquals(dataTwo, result2.objectForKey("Frank"));
        Assert.assertEquals(new NSSet<String>(new String[] { "Bob", "Frank" }), new NSSet<String>(result2.allKeys()));

        NSMutableArray<NSDictionary<String,String>> list2 = new NSMutableArray<NSDictionary<String,String>>();
        list2.add(dataOne);
        list2.add(dataTwo);
        list2.add(dataThree);

        try {
            @SuppressWarnings("unused")
			NSDictionary<String,NSDictionary<String,String>> result3 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list2, "name", true);
            Assert.fail();
        } catch (java.lang.RuntimeException re) { /* ok */ }

        NSDictionary<String,NSDictionary<String,String>> result4 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list2, "name", false);

        Assert.assertEquals(dataOne, result4.objectForKey("Bob"));
        Assert.assertEquals(dataThree, result4.objectForKey("Frank"));
        Assert.assertEquals(new NSSet<String>(new String[] { "Bob", "Frank" }), new NSSet<String>(result4.allKeys()));

        NSMutableDictionary<String,Object> job1 = new NSMutableDictionary<String,Object>();
        job1.setObjectForKey("processor", "jobTitle");
        job1.setObjectForKey(dataOne, "employee");

        NSMutableDictionary<String,Object> job2 = new NSMutableDictionary<String,Object>();
        job2.setObjectForKey("boss", "jobTitle");
        job2.setObjectForKey(dataTwo, "employee");

        NSMutableDictionary<String,Object> job3 = new NSMutableDictionary<String,Object>();
        job3.setObjectForKey("flunky", "jobTitle");
        job3.setObjectForKey(dataThree, "employee");

        NSMutableArray<NSDictionary<String,Object>> list3 = new NSMutableArray<NSDictionary<String,Object>>();
        list3.add(job1);
        list3.add(job2);

        NSDictionary<String,NSDictionary<String,Object>> result5 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list3, "employee.name", true);

        Assert.assertEquals(job1, result5.objectForKey("Bob"));
        Assert.assertEquals(job2, result5.objectForKey("Frank"));
        Assert.assertEquals(new NSSet<String>(new String[] { "Bob", "Frank" }), new NSSet<String>(result5.allKeys()));

        NSDictionary<String,NSDictionary<String,Object>> result6 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list3, "employee.name", false);

        Assert.assertEquals(job1, result6.objectForKey("Bob"));
        Assert.assertEquals(job2, result6.objectForKey("Frank"));
        Assert.assertEquals(new NSSet<String>(new String[] { "Bob", "Frank" }), new NSSet<String>(result6.allKeys()));

        NSMutableArray<NSDictionary<String,Object>> list4 = new NSMutableArray<NSDictionary<String,Object>>();
        list4.add(job1);
        list4.add(job2);
        list4.add(job3);

        try {
            @SuppressWarnings("unused")
			NSDictionary<String,NSDictionary<String,Object>> result7 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list4, "employee.name", true);
            Assert.fail();
        } catch (java.lang.RuntimeException re) { /* ok */ }

        NSDictionary<String,NSDictionary<String,Object>> result8 = ERXArrayUtilities.dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(list4, "employee.name", false);

        Assert.assertEquals(job1, result8.objectForKey("Bob"));
        Assert.assertEquals(job3, result8.objectForKey("Frank"));
        Assert.assertEquals(new NSSet<String>(new String[] { "Bob", "Frank" }), new NSSet<String>(result8.allKeys()));
    }

    public void testArrayBySelectingInstancesOfClass() {
        // public static com.webobjects.foundation.NSArray arrayBySelectingInstancesOfClass(com.webobjects.foundation.NSArray, java.lang.Class);
    }

    public void testSortedArrayUsingComparator() {
        // public static com.webobjects.foundation.NSArray sortedArrayUsingComparator(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSComparator);
    }

    public void testArrayWithObjectsSwapped() {
        Assert.assertEquals(new NSArray<String>(new String[] { "one", "three", "two" }),
                            ERXArrayUtilities.arrayWithObjectsSwapped(new NSArray<String>(new String[] { "one", "two", "three" }), "two", "three"));

        // public static com.webobjects.foundation.NSArray arrayWithObjectsSwapped(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.Object);
    }

    public void testArrayWithObjectsAtIndexesSwapped() {
        // public static com.webobjects.foundation.NSArray arrayWithObjectsAtIndexesSwapped(com.webobjects.foundation.NSArray, int, int);
    }

    public void testSwapObjectsInArray() {
        // public static void swapObjectsInArray(com.webobjects.foundation.NSMutableArray, java.lang.Object, java.lang.Object);
    }

    public void testSwapObjectsAtIndexesInArray() {
        // public static void swapObjectsAtIndexesInArray(com.webobjects.foundation.NSMutableArray, int, int);
    }

    public void testSwapObjectWithObjectAtIndexInArray() {
        // public static void swapObjectWithObjectAtIndexInArray(com.webobjects.foundation.NSMutableArray, java.lang.Object, int);
    }

    public void testdeepCloneNSArray() {
        // public static com.webobjects.foundation.NSArray deepClone(com.webobjects.foundation.NSArray, boolean);
    }

    public void testdeepCloneNSSet() {
        // public static com.webobjects.foundation.NSSet deepClone(com.webobjects.foundation.NSSet, boolean);
    }
    
    public void testStdDev() {
    	String numKey = "num";
    	NSDictionary<String, Integer> uno = new NSDictionary<String, Integer>(Integer.valueOf(1), numKey);
    	NSDictionary<String, Integer> dos = new NSDictionary<String, Integer>(Integer.valueOf(2), numKey);
    	NSDictionary<String, Integer> tres = new NSDictionary<String, Integer>(Integer.valueOf(3), numKey);
    	NSDictionary<String, Integer> quatro = new NSDictionary<String, Integer>(Integer.valueOf(4), numKey);
    	NSDictionary<String, Integer> cinco = new NSDictionary<String, Integer>(Integer.valueOf(5), numKey);
    	NSArray<NSDictionary<String, Integer>> numbers = new NSArray<NSDictionary<String,Integer>>(uno, dos, tres, quatro, cinco);
    	BigDecimal pop = ERXValueUtilities.bigDecimalValue(ERXArrayUtilities.stdDev(numbers, numKey, true));
    	assertTrue(BigDecimal.valueOf(Math.sqrt(2)).compareTo(pop) == 0);
    	BigDecimal samp = ERXValueUtilities.bigDecimalValue(ERXArrayUtilities.stdDev(numbers, numKey, false));
    	assertTrue(BigDecimal.valueOf(Math.sqrt(2.5)).compareTo(samp) == 0);
    }
}
