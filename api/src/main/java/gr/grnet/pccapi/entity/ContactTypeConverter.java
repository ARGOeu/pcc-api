package gr.grnet.pccapi.entity;
import gr.grnet.pccapi.enums.ContactType;
import gr.grnet.pccapi.exception.InternalServerErrorException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter
public class ContactTypeConverter implements AttributeConverter<ContactType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ContactType status) {

        if (status == null)
            return null;

        switch (status) {
            case ADMIN:
                return 1;

            case OPERATIONS:
                return 2;

            case SECURITY:
                return 3;
            case OTHER:
                return 4;
            default:
                throw new InternalServerErrorException(status + " not supported.", 501);
        }
    }

    @Override
    public ContactType convertToEntityAttribute(Integer dbData) {

        if (dbData == null)
            return null;

        switch (dbData) {
            case 1:
                return ContactType.ADMIN;

            case 2:
                return ContactType.OPERATIONS;

            case 3:
                return ContactType.SECURITY;
            case 4:
                return ContactType.OTHER;

            default:
                throw new InternalServerErrorException(dbData + " not supported.", 501);
        }
    }
}
