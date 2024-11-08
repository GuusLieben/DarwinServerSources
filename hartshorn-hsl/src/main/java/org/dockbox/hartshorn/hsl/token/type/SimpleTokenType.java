/*
 * Copyright 2019-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dockbox.hartshorn.hsl.token.type;

import java.util.Arrays;
import java.util.Objects;
import org.dockbox.hartshorn.hsl.token.TokenCharacter;

/**
 * Represents a simple token type, which is a basic implementation of the {@link TokenType} interface.
 * This can be used to create custom token types, or to further extend the functionality of existing
 * token types.
 *
 * <p>Simple token types are immutable and can be created using the {@link SimpleTokenType#builder()} method.
 *
 * @param tokenName the name of the token type
 * @param representation the representation of the token type
 * @param keyword whether the token type is a keyword
 * @param standaloneStatement whether the token type is a standalone statement
 * @param reserved whether the token type is reserved
 * @param assignsWith the token type that this token type assigns with
 * @param defaultLexeme the default lexeme of the token type
 * @param characters the characters that the token type can be represented with
 *
 * @see TokenType
 *
 * @since 0.6.0
 *
 * @author Guus Lieben
 */
public record SimpleTokenType(
        String tokenName,
        String representation,
        boolean keyword,
        boolean standaloneStatement,
        boolean reserved,
        TokenType assignsWith,
        String defaultLexeme,
        TokenCharacter[] characters
) implements TokenType {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SimpleTokenType that = (SimpleTokenType) o;
        return this.keyword == that.keyword
            && this.standaloneStatement == that.standaloneStatement
            && this.reserved == that.reserved
            && Objects.equals(this.tokenName, that.tokenName)
            && Objects.equals(this.representation, that.representation)
            && Objects.equals(this.assignsWith, that.assignsWith)
            && Objects.equals(this.defaultLexeme, that.defaultLexeme)
            && Arrays.equals(this.characters, that.characters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.tokenName, this.representation, this.keyword, this.standaloneStatement, this.reserved, this.assignsWith, this.defaultLexeme);
        result = 31 * result + Arrays.hashCode(this.characters);
        return result;
    }

    /**
     * Creates a new builder for a simple token type.
     *
     * @return the builder
     */
    public static Builder<?> builder() {
        return new Builder<>();
    }

    /**
     * A builder for simple token types.
     *
     * @param <S> the type of the builder
     *
     * @see SimpleTokenType
     *
     * @since 0.6.0
     *
     * @author Guus Lieben
     */
    public static class Builder<S extends Builder<S>> {

        private String tokenName;
        private String representation;
        private boolean keyword;
        private boolean standaloneStatement;
        private boolean reserved;
        private TokenType assignsWith;
        private String defaultLexeme;
        private TokenCharacter[] characters = new TokenCharacter[0];

        /**
         * Sets the name of the token type.
         *
         * @param tokenName the name of the token type
         * @return the builder
         *
         * @see TokenType#tokenName()
         */
        public S tokenName(String tokenName) {
            this.tokenName = tokenName;
            return this.self();
        }

        /**
         * Sets the representation of the token type.
         *
         * @param representation the representation of the token type
         * @return the builder
         *
         * @see TokenType#representation()
         */
        public S representation(String representation) {
            this.representation = representation;
            return this.self();
        }

        /**
         * Sets whether the token type is a keyword.
         *
         * @param keyword whether the token type is a keyword
         * @return the builder
         *
         * @see TokenType#keyword()
         */
        public S keyword(boolean keyword) {
            this.keyword = keyword;
            return this.self();
        }

        /**
         * Sets whether the token type is a standalone statement.
         *
         * @param standaloneStatement whether the token type is a standalone statement
         * @return the builder
         *
         * @see TokenType#standaloneStatement()
         */
        public S standaloneStatement(boolean standaloneStatement) {
            this.standaloneStatement = standaloneStatement;
            return this.self();
        }

        /**
         * Sets whether the token type is reserved.
         *
         * @param reserved whether the token type is reserved
         * @return the builder
         *
         * @see TokenType#reserved()
         */
        public S reserved(boolean reserved) {
            this.reserved = reserved;
            return this.self();
        }

        /**
         * Sets the token type that this token type assigns with.
         *
         * @param assignsWith the token type that this token type assigns with
         * @return the builder
         *
         * @see TokenType#assignsWith()
         */
        public S assignsWith(TokenType assignsWith) {
            this.assignsWith = assignsWith;
            return this.self();
        }

        /**
         * Sets the default lexeme of the token type.
         *
         * @param defaultLexeme the default lexeme of the token type
         * @return the builder
         *
         * @see TokenType#defaultLexeme()
         */
        public S defaultLexeme(String defaultLexeme) {
            this.defaultLexeme = defaultLexeme;
            return this.self();
        }

        /**
         * Sets the characters that the token type can be represented with.
         *
         * @param characters the characters that the token type can be represented with
         * @return the builder
         *
         * @see TokenType#characters()
         */
        public S characters(TokenCharacter... characters) {
            this.characters = characters;
            if (this.representation == null) {
                StringBuilder builder = new StringBuilder();
                for (TokenCharacter character : characters) {
                    builder.append(character.character());
                }
                this.representation = builder.toString();
            }
            return this.self();
        }

        protected S self() {
            return (S) this;
        }

        /**
         * Builds the simple token type.
         *
         * @return the simple token type
         */
        public TokenType build() {
            return new SimpleTokenType(
                    this.tokenName,
                    this.representation,
                    this.keyword,
                    this.standaloneStatement,
                    this.reserved,
                    this.assignsWith,
                    this.defaultLexeme,
                    this.characters
            );
        }
    }
}
