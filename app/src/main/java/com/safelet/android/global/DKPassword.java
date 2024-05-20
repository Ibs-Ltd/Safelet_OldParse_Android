/**
 * Created by Badea Mihai Bogdan on Sep 29, 2014
 * Copyright (c) 2014 XLTeam. All rights reserved.
 */
package com.safelet.android.global;

/**
 * Verifies password strength
 * <br/>
 * Java port from https://github.com/masterrr/DKPassword
 */
public final class DKPassword {

    private String pass;

    /**
     * Calculates the password strength
     *
     * @param password Password to be tested
     * @return Password strength in percentage 0 lowest strength, 100 best strength
     */
    public static int passwordStrength(String password) {
        DKPassword p = new DKPassword();
        p.pass = password;
        return p.mark();
    }

    private int countLetterCharset(String set) {
        int count = 0;
        for (int i = 0; i < pass.length(); i++) {
            if (set.contains(String.valueOf(pass.charAt(i)))) {
                count++;
            }
        }
        return count;
    }

    private int symbolsCount() {
        return countLetterCharset(")!@#$%^&*()");
    }

    private int numbersCount() {
        return countLetterCharset("0123456789");
    }

    private int uppercaseCount() {
        return countLetterCharset("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    private int lowercaseCount() {
        return countLetterCharset("abcdefghijklmnopqrstuvwxyz");
    }

    private int lettersCount() {
        return countLetterCharset("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    private boolean numbersOnly() {
        return numbersCount() == pass.length();
    }

    private boolean lettersOnly() {
        return lettersCount() == pass.length();
    }

    private int lettersOnlyScore() {
        return lettersOnly() ? lettersCount() : 0;
    }

    private int numbersOnlyScore() {
        return numbersOnly() ? numbersCount() : 0;
    }

    private int numberOfCharactersScore() {
        return pass.length() * 4;
    }

    private int lowercaseLetterScore() {
        return (uppercaseCount() > 0 && lowercaseCount() > 0) ? (pass.length() - lowercaseCount()) * 2 : 0;
    }

    private int uppercaseLetterScore() {
        return (uppercaseCount() > 0 && lowercaseCount() > 0) ? (pass.length() - uppercaseCount()) * 2 : 0;
    }

    private int numbersScore() {
        return numbersOnly() ? 0 : numbersCount() * 4;
    }

    @SuppressWarnings("unused")
    private int symbolsScore() {
        return symbolsCount() * 6;
    }

    private int additions() {
        int count = 0;
        count += numberOfCharactersScore() + uppercaseLetterScore() + lowercaseLetterScore() + numbersScore();
        return count;
    }

    private int deductions() {
        int count = 0;
        count += numbersOnlyScore() + lettersOnlyScore();
        return count;
    }

    private int mark() {
        int mark = additions() - deductions();
        return mark > 100 ? 100 : mark;
    }
}
