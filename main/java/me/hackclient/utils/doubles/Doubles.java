package me.hackclient.utils.doubles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Doubles<A, B> {
	A first;
	B second;
}
