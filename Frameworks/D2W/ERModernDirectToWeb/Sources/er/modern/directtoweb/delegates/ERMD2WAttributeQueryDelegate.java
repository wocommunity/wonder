package er.modern.directtoweb.delegates;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Delegate handling search values that are to be applied to multiple
 * attributes, e.g. id, email and date. Components need to implement the
 * {@link ERMD2WQueryComponent} interface. One or more attributes to qualify on
 * may be defined via the searchKey D2W key. If searchKey is null,
 * keyWhenRelationship will be evaluated.
 * 
 * If you wish to search for dates, you likely want to specify custom patterns
 * via
 * er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate.datePatterns.
 * The default patterns are for use with ERXTimestampFormatter's default of
 * month, day, year, separated by '/'.<br>
 * If "ddMM" or "MMdd" are defined, a four digit query string will be
 * interpreted as a day/month combination. If these patterns are not defined,
 * but "yyyy" is, then a four digit query string will be interpreted as a year.
 * 
 * For usage examples, see @ERMD2WListFilter and @ERMD2WEditToOneTypeAhead.
 * 
 * @property 
 *           er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate.datePatterns
 * 
 * @author fpeters
 */
public class ERMD2WAttributeQueryDelegate {
    private static final Logger log = LoggerFactory.getLogger(ERMD2WAttributeQueryDelegate.class);

    public static final ERMD2WAttributeQueryDelegate instance = new ERMD2WAttributeQueryDelegate();

    /**
     * Simple interface required for use of {@link ERMD2WAttributeQueryDelegate}
     * 
     */
    public interface ERMD2WQueryComponent {

        public String searchValue();

        public D2WContext d2wContext();

        public EODataSource dataSource();
    }

    public EOQualifier buildQualifier(ERMD2WQueryComponent sender) {
        EOQualifier qualifier = null;
        Integer typeAheadMinimumCharacterCount = ERXValueUtilities
                .IntegerValueWithDefault(
                        sender.d2wContext().valueForKey("typeAheadMinimumCharacterCount"),
                        3);
        if (sender.searchValue() != null
                && sender.searchValue().length() >= typeAheadMinimumCharacterCount) {

            NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
            for (String anAttributeName : searchKey(sender)) {
                EOEntity entity = null;
                if (sender.dataSource() != null
                        && sender.dataSource() instanceof EODatabaseDataSource) {
                    entity = ((EODatabaseDataSource) sender.dataSource()).entity();
                } else {
                    // sender should be a to-one relationship component
                    entity = EOModelGroup.defaultGroup().entityNamed(
                            (String) sender.d2wContext().valueForKey(
                                    "destinationEntityName"));
                }
                EOAttribute attribute = entity.attributeNamed(anAttributeName);
                if (attribute == null) {
                    attribute = resolveDestinationAttribute(anAttributeName, entity,
                            attribute);
                }

                if (attribute != null) {
                    String attributeClassName = attribute.className();
                    if ("java.lang.Number".equals(attributeClassName)) {
                        buildNumberQualifier(sender, qualifiers, anAttributeName);
                    } else if ("java.math.BigDecimal".equals(attributeClassName)) {
                        buildBigDecimalQualifier(sender, qualifiers, anAttributeName);
                    } else if (attributeClassName.toLowerCase().contains("enum")) {
                        buildEnumQualifier(sender, qualifiers, anAttributeName,
                                attributeClassName);
                    } else if ("com.webobjects.foundation.NSTimestamp"
                            .equals(attributeClassName)) {
                        buildDateQualifier(sender, qualifiers, anAttributeName);
                    } else {
                        qualifiers.addObject(new EOKeyValueQualifier(anAttributeName,
                                selector, "*" + sender.searchValue() + "*"));
                    }
                }
            }
            qualifier = ERXQ.or(qualifiers);
        }
        // handle an existing extra qualifier
        EOQualifier extraRestrictingQ = (EOQualifier) sender.d2wContext().valueForKey(
                "extraRestrictingQualifier");
        if (extraRestrictingQ != null) {
            qualifier = ERXQ.and(qualifier, extraRestrictingQ);
        }
        return qualifier;
    }

    private static final NSSelector<?> selector = EOQualifier.QualifierOperatorCaseInsensitiveLike;

    @SuppressWarnings("unchecked")
    private NSArray<String> searchKey(ERMD2WQueryComponent sender) {
        NSArray<String> searchKey = null;
        if (sender.d2wContext().valueForKey("searchKey") == null
                && sender.dataSource() != null
                && sender.dataSource() instanceof EODatabaseDataSource) {
            // fallback, choose first attribute
            searchKey = new NSArray<String>(
                    (String) ((EODatabaseDataSource) sender.dataSource()).entity()
                            .classPropertyNames().objectAtIndex(0));
        } else if (sender.d2wContext().valueForKey("searchKey") == null
                && sender.d2wContext().valueForKey("keyWhenRelationship") != null
                && sender.d2wContext().valueForKey("keyWhenRelationship") instanceof String) {
            searchKey = new NSArray<String>((String) sender.d2wContext().valueForKey(
                    "keyWhenRelationship"));
        } else if (sender.d2wContext().valueForKey("searchKey") instanceof String) {
            searchKey = new NSArray<String>((String) sender.d2wContext().valueForKey(
                    "searchKey"));
        } else {
            searchKey = (NSArray<String>) sender.d2wContext().valueForKey("searchKey");
        }
        return searchKey;
    }

    private void buildDateQualifier(ERMD2WQueryComponent sender,
                                    NSMutableArray<EOQualifier> qualifiers,
                                    String anAttributeName) {
        // don't attempt to parse when the string's too short or too long
        if (sender.searchValue().length() > 3 && sender.searchValue().length() < 11) {
            try {
                // same default as ERXTimestampformatter: month, day, year
                // separated by '/'
                NSArray<String> defaultPatterns = new NSArray<String>("MM/dd", "MM/dd/",
                        "MM/dd/yy", "MM/DD/yyyy", "yyyy");
                @SuppressWarnings("unchecked")
                NSArray<String> patterns = ERXProperties
                        .arrayForKeyWithDefault(
                                "er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate.datePatterns",
                                defaultPatterns);

                // prepare a parser for the given patterns
                DateTimeParser[] parsers = new DateTimeParser[patterns.count()];
                for (int i = 0; i < patterns.count(); i++) {
                    parsers[i] = DateTimeFormat.forPattern(patterns.objectAtIndex(i))
                            .getParser();
                }
                DateTimeFormatter parsingFormatter = new DateTimeFormatterBuilder()
                        .append(null, parsers).toFormatter();

                // attempt parse the search value as a date
                DateTime date = parsingFormatter.parseDateTime(sender.searchValue());

                // if a parser w/o a year was applied, the date will default to
                // 1970 or 2000, which we modify to the current year
                if (date != null && date.getYear() == 1970
                        && !sender.searchValue().contains("70")) {
                    date = date.plusYears(new DateTime().getYear() - 1970);
                } else if (date != null && date.getYear() == 2000
                        && !sender.searchValue().contains("00")) {
                    date = date.plusYears(new DateTime().getYear() - 2000);
                }
                // should the query string be interpreted as a year?
                if (!(patterns.contains("ddMM") || patterns.contains("MMdd"))
                        && patterns.contains("yyyy")
                        && sender.searchValue().matches("^\\d{4}$")) {
                    // search for a whole year
                    EOQualifier dateQ = ERXQ.between(
                            anAttributeName,
                            new NSTimestamp(date.minusHours(date.getHourOfDay())
                                    .getMillis()),
                            new NSTimestamp(date.plusYears(1)
                                    .minusHours(date.getHourOfDay()).getMillis()));
                    qualifiers.addObject(dateQ);
                }
                // no, search for whole days, i.e. 0-24 hours
                else if (date != null && date.getYear() > 1950) {
                    EOQualifier dateQ = ERXQ.between(
                            anAttributeName,
                            new NSTimestamp(date.minusHours(date.getHourOfDay())
                                    .getMillis()),
                            new NSTimestamp(date.plusDays(1)
                                    .minusHours(date.getHourOfDay()).getMillis()));
                    qualifiers.addObject(dateQ);
                }
            } catch (IllegalArgumentException iae) {
                log.debug("Failed to prepare date qualifier:", iae);
            }
        }
    }

    private void buildEnumQualifier(ERMD2WQueryComponent sender,
                                    NSMutableArray<EOQualifier> qualifiers,
                                    String anAttributeName,
                                    String attributeClassName) {
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> klass = _NSUtilities.classWithName(attributeClassName);
        NSMutableArray<Enum<?>> matchingEnums = new NSMutableArray<Enum<?>>();
        if (klass != null && klass.isEnum()) {
            Enum<?>[] em = klass.getEnumConstants();
            for (int i = 0, length = em.length; i < length; ++i) {
                Enum<?> anEnum = em[i];
                // localizing, to make sure the value is in the cache
                ERXLocalizer.currentLocalizer().localizedStringForKey(anEnum.name());
                String unlocalizedName = (String) ERXLocalizer.currentLocalizer().cache()
                        .objectForKey(anEnum.name());
                if (unlocalizedName.toLowerCase().contains(
                        sender.searchValue().toLowerCase())) {
                    matchingEnums.addObject(anEnum);
                }
            }
        }
        for (Enum<?> anEnum : matchingEnums) {
            qualifiers
                    .addObject(new EOKeyValueQualifier(anAttributeName, ERXQ.EQ, anEnum));
        }
    }

    private void buildBigDecimalQualifier(ERMD2WQueryComponent sender,
                                          NSMutableArray<EOQualifier> qualifiers,
                                          String anAttributeName) {
        Number numericValue = null;
        try {
            numericValue = new BigDecimal(sender.searchValue());
            qualifiers.addObject(new EOKeyValueQualifier(anAttributeName,
                    EOQualifier.QualifierOperatorEqual, numericValue));
        } catch (NumberFormatException nfe) {
        }
    }

    private void buildNumberQualifier(ERMD2WQueryComponent sender,
                                      NSMutableArray<EOQualifier> qualifiers,
                                      String anAttributeName) {
        Number numericValue = null;
        try {
            numericValue = Integer.valueOf(sender.searchValue());
            qualifiers.addObject(new EOKeyValueQualifier(anAttributeName,
                    EOQualifier.QualifierOperatorEqual, numericValue));
        } catch (NumberFormatException nfe) {
        }
    }

    private EOAttribute resolveDestinationAttribute(String anAttributeName,
                                                    EOEntity entity,
                                                    EOAttribute attribute) {
        /*
         * we have a relationship here, let's find the destination attribute
         */
        EORelationship relationship = entity.anyRelationshipNamed(ERXStringUtilities
                .keyPathWithoutLastProperty(anAttributeName));
        if (relationship == null) {
            relationship = entity.anyRelationshipNamed(ERXStringUtilities
                    .firstPropertyKeyInKeyPath(anAttributeName));
            if (relationship != null) {
                // this is a multi-hop relationship, recurse to resolve it
                attribute = resolveDestinationAttribute(
                        ERXStringUtilities.keyPathWithoutFirstProperty(anAttributeName),
                        relationship.destinationEntity(), attribute);
            } else {
                log.warn("Failed to resolve destination attribute for key path: {}",
                        anAttributeName);
            }
        } else {
            entity = relationship.destinationEntity();
            if (entity != null) {
                String resolvedAttributeName = ERXStringUtilities
                        .lastPropertyKeyInKeyPath(anAttributeName);
                attribute = entity.attributeNamed(resolvedAttributeName);
                // TODO set distinct as this may be a to-many
            }
        }
        return attribute;
    }
}
