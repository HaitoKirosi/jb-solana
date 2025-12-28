use std::env;

fn main() {
    println!("Hello from Cargo Project!");

    let args: Vec<String> = env::args().collect();
    if args.len() > 1 {
        println!("Arguments received:");
        for (i, arg) in args.iter().enumerate().skip(1) {
            println!("  Arg {}: {}", i, arg);
        }
    } else {
        println!("No arguments received.");
    }
}
