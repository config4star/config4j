PROJECT_ROOT=../..
include $(PROJECT_ROOT)/src/Makefile.inc

OBJ_FILES=	main.o
APP_NAME=	test-schema-types
CFG_FILE=	schema-type-tests.cfg

.PHONY: all clean compile run-tests

all:		compile run-tests

compile:	$(APP_NAME)

$(APP_NAME):	$(OBJ_FILES)
	$(CXX) -o $(APP_NAME) $(OBJ_FILES) $(LINK_WITH_CONFIG4CPP)

run-tests:	
		$(APP_NAME) -cfg $(CFG_FILE)

valgrind:	
		valgrind --suppressions=valgrind.supp $(APP_NAME) -cfg $(CFG_FILE)

clean:
	rm -f *.o $(APP_NAME)


