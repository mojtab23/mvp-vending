package com.javidconsulting.mvpvending.validation

import org.passay.*
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass


@MustBeDocumented
@Constraint(validatedBy = [PasswordConstraintValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidPassword(
    val message: String = "Invalid Password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordConstraintValidator : ConstraintValidator<ValidPassword?, String?> {
    override fun initialize(arg0: ValidPassword?) {}

    override fun isValid(password: String?, context: ConstraintValidatorContext): Boolean {

        //customizing validation messages
//        val props = Properties()
//        val inputStream = javaClass
//            .classLoader.getResourceAsStream("passay.properties")
//        props.load(inputStream)
//        val resolver: MessageResolver = PropertiesMessageResolver(props)
        val validator = PasswordValidator(
            listOf( // length between 8 and 16 characters
                LengthRule(8, 16),  // at least one upper-case character
                CharacterRule(EnglishCharacterData.UpperCase, 1),  // at least one lower-case character
                CharacterRule(EnglishCharacterData.LowerCase, 1),  // at least one digit character
                CharacterRule(EnglishCharacterData.Digit, 1),  // at least one symbol (special character)
                CharacterRule(EnglishCharacterData.Special, 1),  // no whitespace
                WhitespaceRule(),  // rejects passwords that contain a sequence of >= 5 characters alphabetical  (e.g. abcdef)
                IllegalSequenceRule(
                    EnglishSequenceData.Alphabetical,
                    5,
                    false
                ),  // rejects passwords that contain a sequence of >= 5 characters numerical   (e.g. 12345)
                IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false)
            )
        )
        val result: RuleResult = validator.validate(PasswordData(password))
        if (result.isValid) {
            return true
        }
        val messages: List<String> = validator.getMessages(result)
        val messageTemplate = java.lang.String.join(",", messages)
        context.buildConstraintViolationWithTemplate(messageTemplate)
            .addConstraintViolation()
            .disableDefaultConstraintViolation()
        return false
    }
}
