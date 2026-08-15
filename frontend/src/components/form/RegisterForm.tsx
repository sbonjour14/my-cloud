import {
    FieldGroup,
    FieldDescription,
    FieldLabel,
    Field,
    FieldError,
} from "@/components/ui/field";

import { useForm } from "react-hook-form";
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from "../ui/input-group";
import { Eye, EyeOff, Mail, UserIcon } from "lucide-react";
import { useState } from "react";
import type { RegisterFormBody } from "@/types";
import { Button } from "../ui/button";
import { toast } from "../ui/toast";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "@/lib/http-api/auth";
import axios from "axios";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card";
import { cn } from "@/lib/utils";

const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface RegisterFormValues extends RegisterFormBody {
    confirmPassword: string;
}

export function RegisterForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const {
        register,
        handleSubmit,
        watch,
        clearErrors,
        setError,
        reset,
        formState: { errors },
    } = useForm<RegisterFormValues>({
        mode: "onSubmit",
        reValidateMode: "onSubmit",
    });

    const navigate = useNavigate();

    const [isPasswordShown, setIsPasswordShown] = useState<boolean>(false);
    const [isConfirmPasswordShown, setIsConfirmPasswordShown] = useState<boolean>(false);

    const passwordInputType = isPasswordShown ? "text" : "password";
    const confirmPasswordInputType = isConfirmPasswordShown ? "text" : "password";

    const togglePassword = () => setIsPasswordShown((p) => !p);
    const toggleConfirmPassword = () => setIsConfirmPasswordShown((p) => !p);

    const password = watch("password");

    const onSubmit = async (data: RegisterFormValues) => {
        const { confirmPassword, ...payload } = data;
        try {
            await registerUser(payload);
            const id = toast.add({
                type: "success",
                description: "Account created with success",
                actionProps: {
                    children: "go to login",
                    onClick() {
                        navigate("/login");
                        toast.close(id);
                    },
                },
            });
            reset();
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                setError("root", {
                    type: "manual",
                    message: error.response?.data?.message || "An error occurred during registration.",
                });
                return;
            }

            setError("root", {
                type: "manual",
                message: "An unexpected error occurred",
            });
        }
    };

    return (
        <div className={cn("flex flex-col gap-6 px-2", className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Create your account</CardTitle>
                    <CardDescription>
                        Set up your personal cloud space in seconds.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} onChange={() => clearErrors("root")}>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="email">Email</FieldLabel>
                                <InputGroup>
                                    <InputGroupAddon align={"inline-start"}>
                                        <Mail />
                                    </InputGroupAddon>
                                    <InputGroupInput
                                        type="text"
                                        id="email"
                                        placeholder="Enter your email"
                                        {...register("email", {
                                            required: "Email is required",
                                            pattern: {
                                                value: emailRegex,
                                                message: "Invalid email format",
                                            },
                                            onChange: () => clearErrors("email"),
                                        })}
                                        aria-invalid={!!errors.email}
                                    />
                                </InputGroup>
                                {!!errors.email && <FieldError>{errors.email?.message as string}</FieldError>}
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="username">Username</FieldLabel>
                                <InputGroup>
                                    <InputGroupAddon align={"inline-start"}>
                                        <UserIcon />
                                    </InputGroupAddon>
                                    <InputGroupInput
                                        type="text"
                                        id="username"
                                        placeholder="Enter your username"
                                        {...register("displayName", {
                                            required: "Username is required",
                                            minLength: {
                                                value: 3,
                                                message: "Username must be at least 3 characters",
                                            },
                                            onChange: () => clearErrors("displayName"),
                                        })}
                                        aria-invalid={!!errors.displayName}
                                    />
                                </InputGroup>
                                {!!errors.displayName && <FieldError>{errors.displayName?.message as string}</FieldError>}
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="password">Password</FieldLabel>
                                <InputGroup>
                                    <InputGroupInput
                                        type={passwordInputType}
                                        id="password"
                                        placeholder="Enter your password"
                                        {...register("password", {
                                            required: "Password is required",
                                            minLength: {
                                                value: 8,
                                                message: "Password must be at least 8 characters",
                                            },
                                            onChange: () => clearErrors("password"),
                                        })}
                                        aria-invalid={!!errors.password}
                                    />
                                    <InputGroupAddon align={"inline-end"}>
                                        <InputGroupButton
                                            type="button"
                                            tabIndex={-1}
                                            aria-label={isPasswordShown ? "hide password" : "show password"}
                                            onClick={togglePassword}
                                        >
                                            {isPasswordShown ? <Eye /> : <EyeOff />}
                                        </InputGroupButton>
                                    </InputGroupAddon>
                                </InputGroup>
                                {!!errors.password && <FieldError>{errors.password?.message as string}</FieldError>}
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="confirm-password">Confirm password</FieldLabel>
                                <InputGroup>
                                    <InputGroupInput
                                        type={confirmPasswordInputType}
                                        id="confirm-password"
                                        placeholder="Confirm your password"
                                        {...register("confirmPassword", {
                                            required: "Please confirm your password",
                                            validate: (value) =>
                                                value === password || "Passwords do not match",
                                            onChange: () => clearErrors("confirmPassword"),
                                        })}
                                        aria-invalid={!!errors.confirmPassword}
                                    />
                                    <InputGroupAddon align={"inline-end"}>
                                        <InputGroupButton
                                            type="button"
                                            tabIndex={-1}
                                            aria-label={isConfirmPasswordShown ? "hide password" : "show password"}
                                            onClick={toggleConfirmPassword}
                                        >
                                            {isConfirmPasswordShown ? <Eye /> : <EyeOff />}
                                        </InputGroupButton>
                                    </InputGroupAddon>
                                </InputGroup>
                                {!!errors.confirmPassword && (
                                    <FieldError>{errors.confirmPassword?.message as string}</FieldError>
                                )}
                            </Field>

                            {!!errors.root && (
                                <p className="text-sm text-red-600 text-center">{errors.root.message}</p>
                            )}

                            <Field>
                                <Button type="submit">Create account</Button>
                                <FieldDescription className="text-center">
                                    Already have an account? <Link to="/login">Login</Link>
                                </FieldDescription>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>
            </Card>
            <FieldDescription className="px-6 text-center">
                By clicking continue, you agree to our <a href="#">Terms of Service</a>{" "}
                and <a href="#">Privacy Policy</a>.
            </FieldDescription>
        </div>
    );
}