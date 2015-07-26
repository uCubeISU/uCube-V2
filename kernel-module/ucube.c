#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/init.h>
#include <linux/fs.h>

#define DRIVER_NAME "uCube"
#define DRIVER_AUTHOR "uCube Iowa State University Development Team"
#define DRIVER_DESC "Driver for the uCube interactive LED cube hardware"
#define DRIVER_LICENSE "GPL"

#define DEBUG_LEVEL KERN_INFO

#define debug(msg,...) pr_debug(DRIVER_NAME ": " msg, ##__VA_ARGS__)

static int MajorNumber;


static int ucube_open(struct inode * inode, struct file * file)
{
	debug("Open the file\n");
	return 0;
}

static int ucube_release(struct inode * inode, struct file * file)
{
	debug("Release the file\n");
	return 0;
}

static ssize_t ucube_read(struct file * file, char __user * buffer, size_t length, loff_t * offset)
{
	debug("Read the file\n");
	return 0;
}

static ssize_t ucube_write(struct file * file, const char * buffer, size_t length, loff_t * offset)
{
	debug("Write the file\n");
	return length;
}

static int ucube_mmap(struct file * file, struct vm_area_struct* map)
{
	return 0;
}

struct file_operations fops = {
	read: ucube_read,
	write: ucube_write,
	open: ucube_open,
	release: ucube_release,
	mmap: ucube_mmap
};


static int __init ucube_init(void)
{
	debug("Hello World\n");
	
	MajorNumber = register_chrdev(0, DRIVER_NAME, &fops);
	if(MajorNumber < 0)
	{
		printk(KERN_ALERT "Registering char device " DRIVER_NAME " failed with %d\n", MajorNumber);
		return MajorNumber;
	}
	debug("Major number = %d\n", MajorNumber);
	return 0;
}

static void __exit ucube_exit(void)
{
	unregister_chrdev(MajorNumber, DRIVER_NAME);
	debug("Goodbye World\n");
}


module_init(ucube_init);
module_exit(ucube_exit);

MODULE_LICENSE(DRIVER_LICENSE);
MODULE_AUTHOR(DRIVER_AUTHOR);
MODULE_DESCRIPTION(DRIVER_DESC);
//MODULE_SUPPORTED_DEVICES("");

