/**
 * This file is part of Erjang - A JVM-based Erlang VM
 *
 * Copyright (c) 2009 by Trifork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package erjang;

import java.io.File;
import java.net.MalformedURLException;

/**
 * This will eventually be the main entrypoint for an OTP node.
 * Loads preloaded erlang modules, and invokes otp_ring0:start/2
 * 
 */
public class OTPMain {

	public static String PRELOADED = "src/main/erl/preloaded/ebin";
	public static String[] MODULES = new String[] { "erl_prim_loader",
			"erlang", "init", "otp_ring0", "prim_file", "prim_inet",
			"prim_zip", "zlib" };

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ClassNotFoundException, MalformedURLException, InstantiationException, IllegalAccessException {
		
		EModule[] modules = new EModule[MODULES.length];
		File preloaded_dir = new File(PRELOADED);
		
		for (int i = 0; i < modules.length; i++) {
			
			String mod = MODULES[i];
			
			// assume preloaded classes are compiled
			File path = new File(preloaded_dir, mod + ".classes");
			
			if (!path.exists() || !path.isDirectory()) {
				throw new Error("no path to: "+path+"\n run java erjang.ErjC "+preloaded_dir+"/"+mod+".beam");
			}
			
			Object o = new kilim.State();
			
			modules[i] = EModule.load_module(EAtom.intern(mod), path.toURI().toURL());
		}

		args=new String[] {"-boot", "/foo/bar", "-root", "/xx/yy"};
		
		
		EAtom ring = EAtom.intern("otp_ring0");
		EAtom am_start = EAtom.intern("start");
		ESeq env = ERT.NIL;
		ESeq argv = ERT.NIL;
		
		for (int i = args.length-1; i >= 0; i--) {
			argv = argv.cons(EBinary.fromString(args[i]));
		}
		
		String s = argv.toString();
		
		EProc proc = new EProc(null, ring, am_start, ERT.NIL.cons(argv).cons(env));

		ERT.run(proc);
		proc.joinb();
		
		System.out.println("done.");
	}
}